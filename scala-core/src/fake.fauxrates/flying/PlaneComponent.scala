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




	/* CODES. BEWARE. */
	def location = locationC
	def location_= (outpost : OutpostComponent) = location = Some(outpost)
	def location_= (outpost : Option[OutpostComponent]) = outpost match {
		case Some(x) =>
			locationC = outpost
			locationId = Some(x.id)
			locationXY = x.XY
		case None =>
			locationC = None
			locationId = None
	}

	def locationXY = (x,y)
	def locationXY_= (t : (Double,Double)) = (x,y) = t
	/* TODO reset location when setting locationXY ? */

	def target = targetC
	def target_= (outpost : OutpostComponent) = target = Some(outpost)
	def target_= (outpost : Option[OutpostComponent]) = outpost match {
		case Some(x) =>
			targetC = outpost
			targetId = Some(x.id)
			targetXY = x.XY
		case None =>
			targetC = None
			targetId = None
	}

	/* TODO there should be a way to disallow getter because it's Option and not what you expect */
	def targetXY = if (targetX.isDefined) Some(targetX.get, targetY.get) else None
	def targetXY_= (t : (Double,Double)) = {
		val (tx,ty) = t
		targetX = Some(tx)
		targetY = Some(ty)
	}
}