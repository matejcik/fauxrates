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
	def location = locationC

	def location_=(outpost: OutpostComponent): Unit = location = Some(outpost)

	def location_=(outpost: Option[OutpostComponent]): Unit = outpost match {
		case Some(x) =>
			locationC = outpost
			locationId = Some(x.id)
			XY = x.XY
		case None =>
			locationC = None
			locationId = None
	}

	def XY = (x, y)

	def XY_=(t: (Double, Double)) = t match {
		case (xx, yy) => x = xx; y = yy
	}
}
