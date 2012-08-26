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


	def target = targetC

	def target_=(outpost: OutpostComponent): Unit = target = Some(outpost)

	def target_=(outpost: Option[OutpostComponent]): Unit = outpost match {
		case Some(x) =>
			targetC = outpost
			targetId = Some(x.id)
			XY = x.XY
		case None =>
			targetC = None
			targetId = None
	}

	def XY = (targetX, targetY)

	def XY_=(t: (Double, Double)): Unit = t match {
		case (xx, yy) => targetX = xx; targetY = yy
	}
}
