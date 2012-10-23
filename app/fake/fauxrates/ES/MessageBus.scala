package fake.fauxrates
package ES

import akka.actor.{Props, Actor}
import play.api.libs.concurrent.Akka
import play.api.Play.current

trait MessageBus {
	private type Receiver = (Any) => Any
	private var listeners = List[Receiver]()

	private class MessageActor extends Actor {
		def receive = {
			case x => resend(x)
		}
	}

	def subscribe(func: Receiver) {
		synchronized {
			listeners ::= func
	} }

	private def resend(m: Any) {
		listeners foreach {
			_(m)
		}
	}

	val sendMsg = Akka.system.actorOf(Props(new MessageActor))
}
