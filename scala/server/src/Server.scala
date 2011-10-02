package fake.fauxrates.server

import fake.fauxrates.packets.Packet
import java.net.InetSocketAddress
import java.nio.channels._
import actors.Actor
import collection.immutable.HashMap
import collection.JavaConversions._
import java.nio.ByteBuffer
import java.nio.charset.Charset
import collection.mutable.SynchronizedQueue
import org.codehaus.jackson.map.ObjectMapper

abstract class Server (val port : Int) {

	def start () = {
		selectorThread.prepare
		running = true
		director.start()
		selectorThread.start()
	}

	val selector = Selector.open()
	val server = ServerSocketChannel.open()

	val selectorLock = new Object
	var updating = false
	var running = false

	var connections = new HashMap[SelectionKey,Connection]

	private object selectorThread extends Thread {

		def prepare = {
			server.configureBlocking(false)
			server.socket().bind(new InetSocketAddress(port))
			server.register(selector, SelectionKey.OP_ACCEPT)
		}

		override def run () = {
			while (running) {
				selector.select()
				if (updating) selectorLock synchronized {}
				// give the director a chance to modify selection set
				val keys = selector.selectedKeys()
				val it = keys.iterator()
				while (it.hasNext) {
					val key = it.next()
					it.remove()
					matchKey(key)
				}
			}
		}

		def matchKey (key : SelectionKey) = key match {
			case k if k.isValid => Nil
			case k if k.isAcceptable => accept(key)
			case k if k.isReadable => readFrom(key)
			case k if k.isWritable => writeTo(key)
			// TODO check for disconnect in default case
		}

		def accept (key : SelectionKey) = {
			assume(server == key.channel())
			var socket : SocketChannel = null
			while ((socket = server.accept()) != null) {
				socket.configureBlocking(false)
				val key = socket.register(selector, SelectionKey.OP_READ)
				connections.put(key, new Connection(socket))
				// TODO notify director of new connection?
			}
		}

		val buffer = ByteBuffer.allocate(8192)
		val charset = Charset.forName("UTF-8")

		def readFrom (key : SelectionKey) = {
			val socket = key.channel().asInstanceOf[SocketChannel]
			val connection = connections.get(key)
			assume(connection.isDefined)
			buffer.clear()
			socket.read(buffer)
			val decode = charset.decode(buffer).toString
			director ! (connection.get, decode)
		}

		def writeTo (key : SelectionKey) = {
			val socket = key.channel().asInstanceOf[SocketChannel]
			val c = connections.getOrElse(key, null)
			assume(c != null)
			var canWrite = true

			while (canWrite && (c.writeBuffer != null || !c.pendingMessages.isEmpty)) {
				if (c.writeBuffer == null) {
					val m = c.pendingMessages.dequeue
					assume(!m.isEmpty)
					c.writeBuffer = charset.encode(m)
				}
				if (socket.write(c.writeBuffer) == 0) canWrite = false
				if (!c.writeBuffer.hasRemaining)
				        c.writeBuffer = null
			}

			if (canWrite) // IOW nothing in write buffer and no pending messages
				 key.interestOps(SelectionKey.OP_READ)
		}
	}

	private def updateSelectorWith (fun : () => Unit) = {
		selectorLock synchronized {
			updating = true
			selector.wakeup()
			fun
			updating = false
		}
	}

	private object director extends Actor {
		def act = loop { receive {
			case (c : Connection, msg : String) => receive(c, msg)
			case (c : Connection, p : Packet) => send(c, p)
		} }

		def receive (c: Connection, msg: String) = {
			val start = c.readBuffer.length
			// TODO limit this to prevent DoS on long JSON packets

			def processBufferFrom (i: Int): Unit = {
				for (i <- 0 to c.readBuffer.length) msg(i) match {
					case '{' => c.numOpen += 1
					case '}' => {
						c.numOpen -= 1
						assume(c.numOpen >= 0)
						if (c.numOpen == 0) {
							msgReceived(c, c.readBuffer.substring(0, i))
							c.readBuffer.delete(0, i)
							return processBufferFrom(0)
						}
					}
				}
			}

			c.readBuffer ++= msg
			processBufferFrom(start)
		}

		val mapper = new ObjectMapper

		def send (c: Connection, p: Packet) = {
			val msg = mapper.writeValueAsString(p)
			c.pendingMessages += msg
			val key = c.socket.keyFor(selector)
			updateSelectorWith(() => key.interestOps(SelectionKey.OP_WRITE))
		}

		def msgReceived (c: Connection, s: String) = {
			println("message received: " + s)
			val m = mapper.readValue(s, classOf[java.util.Map[String, Object]])
			// TODO
		}
	}

	def onConnect (connection : Connection) : Unit
	def onReceive (connection : Connection, msg : Map[String, Object]) : Unit
	def onDisconnect (connection : Connection) : Unit
}

class Connection (val socket : SocketChannel) {

	val pendingMessages = new SynchronizedQueue[String]
	var writeBuffer : ByteBuffer = null

	val readBuffer = new StringBuilder
	var numOpen = 0
}