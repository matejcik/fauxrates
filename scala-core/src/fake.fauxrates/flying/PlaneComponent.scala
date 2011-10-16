package fake.fauxrates
package flying

import ES._
import org.squeryl.annotations.Transient
import java.sql.Timestamp

class PlaneComponent(
		var x : Double, var y : Double,
		var locationId : Option[EntitySystem.Entity],
		var targetId : Option[EntitySystem.Entity],
		var targetX : Option[Double], var targetY : Option[Double],
		var timeToLand : Option[java.sql.Timestamp],
		var fuel : Double
	) extends Component {

	@Transient var locationC : Option[OutpostComponent] = None
	@Transient var targetC : Option[OutpostComponent] = None

	/* for squeryl, to know types of Some */
	def this () = this(0, 0, Some(-1), Some(-1), Some(0), Some(0), Some(new Timestamp(0)), 100)

	/* DEFAULT CONSTRUCTOR */
	def this (x : Double, y : Double, locationId : EntitySystem.Entity) =
		this(x, y, Some(locationId), None, None, None, None, 100)

	def this (location : OutpostComponent) = {
		this(location.x, location.y, location.id)
		this.location = location
	}



	/* CODES. BEWARE. */
	def location = locationC
	def location_= (outpost : OutpostComponent) : Unit = location = Some(outpost)
	def location_= (outpost : Option[OutpostComponent]) : Unit = outpost match {
		case Some(x) =>
			locationC = outpost
			locationId = Some(x.id)
			XY = x.XY
		case None =>
			locationC = None
			locationId = None
	}

	def XY = (x,y)
	def XY_= (t : (Double,Double)) = t match { case (xx,yy) => x = xx; y = yy }
	/* TODO reset location when setting locationXY ? */

	def target = targetC
	def target_= (outpost : OutpostComponent) : Unit = target = Some(outpost)
	def target_= (outpost : Option[OutpostComponent]) : Unit = outpost match {
		case Some(x) =>
			targetC = outpost
			targetId = Some(x.id)
			targetXY = x.XY
		case None =>
			targetC = None
			targetId = None
	}

	def targetXY : Unit = {}
	def targetXY_? = if (targetX.isDefined) Some(targetX.get, targetY.get) else None
	def targetXY_= (t : (Double,Double)) : Unit = {
		val (tx,ty) = t
		targetX = Some(tx)
		targetY = Some(ty)
	}
	def targetXY_= (t : Option[(Double,Double)]) : Unit = t match {
		case None =>
			targetX = None
			targetY = None
		case Some(c) =>
			targetXY = c
	}
}
