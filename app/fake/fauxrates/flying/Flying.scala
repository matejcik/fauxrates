package fake.fauxrates
package flying

import ES._
import java.util.Calendar
import org.squeryl.PrimitiveTypeMode._
import java.lang.Math

object Flying extends Runnable with MessageBus {

	type Coords = (Double, Double)

	class OutOfRangeException extends Exception

	case class Takeoff(p: PlaneComponent, start: OutpostComponent)

	case class Landing(p: PlaneComponent, end: OutpostComponent)

	val RANGE: Double = 100
	// km
	val SPEED: Double = 245 // km/h

	private val planes = Persistence.tableFor[PlaneComponent]
	private val inflight = Persistence.tableFor[InFlightComponent]
	//private val outposts = Persistence.tableFor[OutpostComponent]

	case class PlaneInFlight(plane: PlaneComponent, inFlight: InFlightComponent)

	private var queue = List[PlaneInFlight]()

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
		if (!inRange(plane, outpost)) throw new OutOfRangeException

		takeoff(plane, outpost)
	}

	private def takeoff(plane: PlaneComponent, outpost: OutpostComponent) {
		/* happens in caller thread */
		val (dist, time) = distanceAndTime(plane, outpost)
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
		synchronized {
			queue = walk(queue)
			notify()
		}
		sendMsg ! Takeoff(plane, src)
	}

	private def land(tuple: PlaneInFlight) { tuple match { case PlaneInFlight(plane, inFlight) =>
		/* happens in event loop */
		plane.location = inFlight.target
		EntitySystem.remove(inFlight)
		EntitySystem.update(plane)

		/* call out */
		sendMsg ! Landing(plane, plane.location.get)
	} }

	private def init() { inTransaction {
		val all = join(inflight, planes.leftOuter) ( (i, p) =>
			select (i, p)
			orderBy (i.timeToLand asc)
			on (i.id === p.map(_.id))
		)
		synchronized {
			queue = all.withFilter ( _ match {
				case (inFlight, None) =>
					EntitySystem.remove(inFlight)
					false
				case _ => true
			}).map ( _ match {
				case (inFlight, Some(plane)) => PlaneInFlight(plane, inFlight)
				case _ => throw new IllegalStateException("the filter before didn't work")
					// to silence a warning about non-exhaustive match
			}).toList
			// OH HELL YEAH
		}
	} }

	override def run() {
		init()
		while (true) {
			// TODO sane ending condition
			val plane = synchronized {
				if (queue.isEmpty) wait()
				while (Calendar.getInstance().getTimeInMillis < queue.head.inFlight.timeToLand.getTime) {
					Thread.sleep(1000)
				}
				val head = queue.head
				queue = queue.tail
				head
			}
			land(plane)
		}
	}

	val thread = new Thread(this)
	thread.start()
}
