package fake.fauxrates
package flying

import ES._
import org.squeryl.annotations.Transient
import java.sql.Timestamp

class PlaneComponent(
			    var x: Double, var y: Double,
			    var locationId: Option[EntitySystem.Entity],
			    var fuel: Double
			    ) extends Component {

	@Transient var locationC: Option[OutpostComponent] = None


	/* for squeryl, to know types of Some */
	def this() = this(0, 0, Some(-1), 100)

	/* DEFAULT CONSTRUCTOR */
	def this(x: Double, y: Double, locationId: EntitySystem.Entity) =
		this(x, y, Some(locationId), 100)

	def this(location: OutpostComponent) = {
		this(location.x, location.y, location.id)
		this.location = location
	}


	/* CODES. BEWARE. */

		// note: setting return type here seems to be a scala compiler bug
		// if the type is not set, the line with "location = " insists that
		// the function is recursive, which it isn't
	def location : Option[OutpostComponent] = locationC match {
		case Some(location) => locationC
		case None if locationId.isDefined =>
			location = EntitySystem.get[OutpostComponent](locationId.get)
			locationC
		case None => None
	}

	def location_=(outpost: OutpostComponent) { location = Some(outpost) }

	def location_=(outpost: Option[OutpostComponent]) { outpost match {
		case Some(n) =>
			locationC = outpost
			locationId = Some(n.id)
			XY = n.XY
		case None =>
			locationC = None
			locationId = None
	} }

	def XY = (x, y)

	def XY_=(t: (Double, Double)) { t match {
		case (xx, yy) => x = xx; y = yy
	} }
}
