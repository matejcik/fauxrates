package fake.fauxrates
package liftweb
package model

import ES._
import flying._
import net.liftweb.actor.LiftActor
import scala.collection.immutable.List

object TrafficModel extends LiftActor with MessageBus {

	Flying receive { x => this ! x }

	private var listeners = List[EntityListener]()

	protected def messageHandler = {
		case Listen(listener) =>
			listeners ::= listener
		case Unlisten(listener) =>
			listeners = listeners filter { _ != listener }
		case Flying.Takeoff(p, s) =>
			listeners foreach { x => if (x.entity == p.id) x ! Flying.Takeoff(p,s) }
		case Flying.Landing(p, s) =>
			listeners foreach { x => if (x.entity == p.id) x ! Flying.Landing(p,s) }
	}
}
