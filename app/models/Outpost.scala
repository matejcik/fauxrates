package models

import fake.fauxrates.flying._
import fake.fauxrates.ES.EntitySystem

case class Outpost (name : String, distance : Double, timeToLand : Int, component : OutpostComponent)

object Outpost {

	def withRanges (plane: PlaneComponent) = EntitySystem.allOf[OutpostComponent] map { outpost =>
		val (dist, time) = Flying.distanceAndTime(plane, outpost)
		Outpost(outpost.name, dist, time, outpost)
	}
}
