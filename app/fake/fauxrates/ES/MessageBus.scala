package fake.fauxrates
package ES

import actors.Actor

trait MessageBus {

	private type Receiver = (Any) => Any
	private var listeners = List[Receiver]()

	def receive(func: Receiver): Unit = synchronized {
		listeners ::= func
	}

	private def resend(m: Any): Unit = {
		listeners foreach {
			_(m)
		}
	}

	object sendMsg extends Actor {
		def act() = loop {
			react {
				case x => resend(x)
			}
		}
	}

	sendMsg.start()
}
