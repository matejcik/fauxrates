package fake.fauxrates
package liftweb
package model

import ES._
import flying._

import org.squeryl.PrimitiveTypeMode._

object Locations {
	private val outposts = Persistence.tableFor[OutpostComponent]

	abstract class IsReachable
	case object CurrentLocation extends IsReachable
	case object OutOfRange extends IsReachable
	case class InRange(seconds : Double) extends IsReachable

	def outpostsWithDistances (plane : PlaneComponent) = {
		val loc = if (plane.locationId.isDefined) plane.locationId.get else -1;
		transaction { from(outposts) { select(_) } map {
			outpost => (outpost, {
				if (loc == outpost.id) CurrentLocation
				else {
					val (dist, time) = Flying.distanceAndTime(plane, outpost)
					if (dist >= Flying.RANGE) OutOfRange
					else InRange(time)
				}
			})
		} }
	}
}
