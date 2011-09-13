package fake.fauxrates.server

import fake.fauxrates.packets.Packet
import java.net.InetSocketAddress
import java.nio.channels._
import actors.Actor
import collection.immutable.HashMap
import collection.JavaConversions._
import java.nio.ByteBuffer
import java.nio.charset.{Charset, CharsetDecoder}

class Server (val port : Int) {

	def start () = {
		selectorThread.prepare
		running = true
		director.start()
		selectorThread.start()
	}

	val selector = Selector.open()
	val server = ServerSocketChannel.open()

	//val selectorLock = new Object
	//var updating = false
	var running = false

	var connections = new HashMap[SelectionKey,Connection]

	private object selectorThread extends Thread {

		def prepare = {
			server.socket().bind(new InetSocketAddress(port))
			server.register(selector, SelectionKey.OP_ACCEPT)
		}

		override def run () = {
			while (running) {
				val count = selector.select()
				//if (updating) selectorLock synchronized {}
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
		}

		def accept (key : SelectionKey) = {
			assume(server == key.channel())
			director ! server.accept()
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
			val connection = connections.get(key)
			assume(connection.isDefined)
		}
	}

	private object director extends Actor {
		def act = {

		}
	}
}