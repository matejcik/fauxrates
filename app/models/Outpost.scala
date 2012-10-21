package models

import fake.fauxrates.flying._
import fake.fauxrates.ES.{Persistence, EntitySystem}
import org.squeryl.PrimitiveTypeMode._

case class Outpost (name : String, distance : Double, timeToLand : Int, component : OutpostComponent)

object Outpost {

	val outposts = Persistence.tableFor[OutpostComponent]

	def withRanges (plane: PlaneComponent) = EntitySystem.allOf[OutpostComponent] map { outpost =>
		val (dist, time) = Flying.distanceAndTime(plane, outpost)
		Outpost(outpost.name, dist, time, outpost)
	}

	def create (name: String, x: Double, y: Double) = transaction {
		outposts.where(o => o.name === name).map ( _ => throw DuplicateException )
		val component = new OutpostComponent(name, x, y)
		EntitySystem.add(EntitySystem.createEntity, component)
		component
	}

	case object DuplicateException extends Exception
}
