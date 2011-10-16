package fake.fauxrates
package liftweb
package model

import net.liftweb.actor.LiftActor
import ES._
import net.liftweb.http.CometActor

case class Listen(e : EntityListener)
case class Unlisten(e : EntityListener)

trait EntityListener extends LiftActor {
	val entity : EntitySystem.Entity
}

trait EntityCometListener extends CometActor with EntityListener {
	override protected def localSetup() : Unit = {
		TrafficModel ! Listen(this)
		super.localSetup()
	}

	override protected def localShutdown() {
		TrafficModel ! Unlisten(this)
		super.localShutdown()
	}
}