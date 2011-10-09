package fake.fauxrates.liftweb.model

import net.liftweb.actor.LiftActor
import collection.immutable.Queue
import net.liftweb.http.ListenerManager
import net.liftweb.common.Logger

object ChatModel extends LiftActor with ListenerManager with Logger {
	type Message = (CharacterComponent, String)

	private var messages = Queue[Message]()

	protected def createUpdate = messages

	override protected def lowPriority = {
		case (c : CharacterComponent, s : String) =>
			info("received message "+s)
			val nm = messages :+ (c, s)
			if (nm.size > 30) messages = nm.drop(nm.size - 30)
			else messages = nm
			updateListeners()
		case x => warn("weird message: " + x)
	}
}