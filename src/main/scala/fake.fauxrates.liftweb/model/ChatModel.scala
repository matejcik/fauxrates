package fake.fauxrates.liftweb.model

import net.liftweb.actor.LiftActor
import collection.immutable.Queue
import net.liftweb.http.ListenerManager
import net.liftweb.common.Logger

object ChatModel extends LiftActor with ListenerManager {
	type Message = (CharacterComponent, String)

	private var messages = Queue[Message]()

	protected def createUpdate = messages

	override protected def lowPriority = {
		case (c : CharacterComponent, s : String) =>
			messages = messages :+ (c, s) take 30
			updateListeners()
	}
}
