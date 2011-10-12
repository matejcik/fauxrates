package fake.fauxrates
package flying

import ES._
import org.squeryl.annotations.Transient

class PlaneComponent(var x : Double, var y : Double, var locationId : Option[EntitySystem.Entity]) extends Component {
	@Transient var locationC : Option[OutpostComponent] = None

	var targetId : Option[EntitySystem.Entity] = None
	@Transient var targetC : Option[OutpostComponent] = None
	var targetX : Option[Double] = None
	var targetY : Option[Double] = None

	var timeToLand : Option[java.sql.Timestamp] = None

	var fuel : Double = 100 // range in km/h




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
