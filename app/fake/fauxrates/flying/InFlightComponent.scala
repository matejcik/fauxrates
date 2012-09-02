package fake.fauxrates.flying

import fake.fauxrates.ES.{EntitySystem, Component}
import org.squeryl.annotations.Transient
import java.sql.Timestamp

class InFlightComponent (
			var targetX : Double, var targetY : Double,
			var targetId : Option[EntitySystem.Entity],
			var timeToLand : Timestamp
		) extends Component {

	@Transient var targetC: Option[OutpostComponent] = None

	def this() = this(0, 0, Some(-1), new Timestamp(0))

	def this(outpost: OutpostComponent, ms: Long) = {
		this()
		target = outpost
		timeToLand = new Timestamp(ms)
	}

		// for why the return type is here, see PlaneComponent.scala
	def target : Option[OutpostComponent] = targetC match {
		case Some(x) => targetC
		case None if targetId.isDefined =>
			target = EntitySystem.get[OutpostComponent](targetId.get)
			targetC
		case None => None
	}

	def target_=(outpost: OutpostComponent) { target = Some(outpost) }

	def target_=(outpost: Option[OutpostComponent]) { outpost match {
		case Some(x) =>
			targetC = outpost
			targetId = Some(x.id)
			XY = x.XY
		case None =>
			targetC = None
			targetId = None
	} }

	def XY = (targetX, targetY)

	def XY_=(t: (Double, Double)) { t match {
		case (xx, yy) => targetX = xx; targetY = yy
	} }
}
