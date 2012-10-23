package fake.fauxrates
package flying

import ES._
import java.util.Calendar
import org.squeryl.PrimitiveTypeMode._
import java.lang.Math
import akka.actor.{Props, Actor}
import akka.util.duration._
import play.api.libs.concurrent.Akka
import play.api.Play.current

object Flying extends MessageBus {

	type Coords = (Double, Double)

	case object OutOfRangeException extends Exception

	// outgoing messages
	case class Takeoff(p: PlaneComponent, start: OutpostComponent)
	case class Landing(p: PlaneComponent, end: OutpostComponent)

	val RANGE: Double = 100
	// km
	val SPEED: Double = 245 // km/h

	//private val outposts = Persistence.tableFor[OutpostComponent]

	def distance(a: Coords, b: Coords): Double = {
		val (ax, ay) = a
		val (bx, by) = b
		val x = (ax - bx)
		val y = (ay - by)
		Math.sqrt(x * x + y * y)
	}

	def distanceAndTime(plane: PlaneComponent, outpost: OutpostComponent): (Double, Int) = {
		val dist = distance(plane.XY, outpost.XY)
		val time = dist / SPEED * 3600 // seconds
		(dist, time.toInt)
	}

	def inRange(plane: PlaneComponent, outpost: OutpostComponent): Boolean =
		distance(plane.XY, outpost.XY) < plane.fuel

	def flyTo(plane: PlaneComponent, outpost: OutpostComponent) {
		if (EntitySystem.get[InFlightComponent](plane.id).isDefined)
			throw new UnsupportedOperationException("no route changes!")
		if (!inRange(plane, outpost)) throw OutOfRangeException

		actor ! FlyTo(plane, outpost)
	}

	def planeFor (id: EntitySystem.Entity) =
		EntitySystem.get[PlaneComponent](id).map { plane =>
		        plane.inFlight = EntitySystem.get[InFlightComponent](id)
			plane.inFlight.map { inflight =>
				plane.location = None
				val (tx, ty) = inflight.XY
				val delta = inflight.timeToLand.getTime - System.currentTimeMillis()
				val dist = (delta / 1000) * SPEED / 3600
				val alpha = Math.atan2(ty - plane.y, tx - plane.x)
				plane.x = tx - dist * Math.cos(alpha)
				plane.y = ty - dist * Math.sin(alpha)
			}
			plane
		}

	private val actor = Akka.system.actorOf(Props[FlyingActor], name = "flyingActor")
	Akka.system.scheduler.schedule(1 seconds, 1 seconds, actor, Tick)
}

private case class FlyTo(plane: PlaneComponent, destination: OutpostComponent)
private case object Tick

private class FlyingActor extends Actor {

	private val planes = Persistence.tableFor[PlaneComponent]
	private val inflight = Persistence.tableFor[InFlightComponent]

	case class PlaneInFlight(plane: PlaneComponent, inFlight: InFlightComponent)

	// internal messages

	private var queue = List[PlaneInFlight]()

	private def takeoff(plane: PlaneComponent, outpost: OutpostComponent) {
		/* happens in caller thread */
		val (dist, time) = Flying.distanceAndTime(plane, outpost)
		val src = plane.location.get

		val ttl = Calendar.getInstance()
		ttl.add(Calendar.SECOND, time.intValue())
		val inflight = new InFlightComponent(outpost, ttl.getTimeInMillis)
		EntitySystem.add(plane.id, inflight)

		val tuple = PlaneInFlight(plane, inflight)

		def walk(q: List[PlaneInFlight]): List[PlaneInFlight] = q match {
			case Nil =>
				tuple :: Nil
			case x :: rest if x.inFlight.timeToLand.getTime > ttl.getTimeInMillis =>
				tuple :: x :: rest
			case x :: rest =>
				x :: walk(rest)
		}
		/* inserts into event loop */
		queue = walk(queue)

		Flying.sendMsg ! Flying.Takeoff(plane, src)
	}

	private def land(tuple: PlaneInFlight) { tuple match { case PlaneInFlight(plane, inFlight) =>
		/* happens in event loop */
		plane.location = inFlight.target
		EntitySystem.remove(inFlight)
		EntitySystem.update(plane)

		/* call out */
		Flying.sendMsg ! Flying.Landing(plane, plane.location.get)
	} }

	override def preStart() { inTransaction {
		val all = join(inflight, planes.leftOuter) ( (i, p) =>
			select (i, p)
			orderBy (i.timeToLand asc)
			on (i.id === p.map(_.id))
		)

		queue = all.withFilter(_ match {
			case (inFlight, None) =>
				EntitySystem.remove(inFlight)
				false
			case _ => true
		}).map(_ match {
			case (inFlight, Some(plane)) => PlaneInFlight(plane, inFlight)
			case _ => throw new IllegalStateException("the filter before didn't work")
			// to silence a warning about non-exhaustive match
		}).toList
		// OH HELL YEAH

	} }

	def receive = {
		case Tick =>
			if (!queue.isEmpty && System.currentTimeMillis() >= queue.head.inFlight.timeToLand.getTime) {
			        land(queue.head)
				queue = queue.tail
		}
		case FlyTo(plane, destination) => takeoff(plane, destination)
		case _ => // nothing
	}
}
