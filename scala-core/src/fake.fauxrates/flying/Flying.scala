package fake.fauxrates
package flying

import ES._
import java.util.Calendar
import java.sql.Timestamp
import org.squeryl.PrimitiveTypeMode._

object Flying extends Thread with MessageBus {

	type Coords = (Double, Double)

	class OutOfRangeException extends Exception

	case class Takeoff(p : PlaneComponent, start : OutpostComponent)
	case class Landing(p : PlaneComponent, end : OutpostComponent)

	val RANGE : Double = 100 // km
	val SPEED : Double = 245 // km/h

	private val planes = Persistence.tableFor[PlaneComponent]
	//private val outposts = Persistence.tableFor[OutpostComponent]

	private var queue = List[PlaneComponent]()

	def filloutLocations (plane : PlaneComponent) {
		if (plane.locationId.isDefined && plane.locationC.isEmpty)
			plane.locationC = EntitySystem.get[OutpostComponent](plane.locationId.get)
		if (plane.targetId.isDefined && plane.targetC.isEmpty)
			plane.targetC = EntitySystem.get[OutpostComponent](plane.targetId.get)
	}

	def distance (a : Coords, b : Coords) : Double = {
		val (ax,ay) = a
		val (bx,by) = b
		val x = (ax - bx)
		val y = (ay - by)
		x * x + y * y
	}

	def distanceAndTime (plane : PlaneComponent, outpost : OutpostComponent) : (Double, Double) = {
		val dist = distance(plane.XY, outpost.XY)
		val time = dist / SPEED * 3600 // seconds
		(dist, time)
	}

	def inRange (plane : PlaneComponent, outpost : OutpostComponent) : Boolean =
		distance(plane.XY, outpost.XY) < plane.fuel

	def flyTo (plane : PlaneComponent, outpost : OutpostComponent) = {
		if (plane.target.isDefined) throw new UnsupportedOperationException("no route changes!")
		if (!inRange(plane, outpost)) throw new OutOfRangeException

		takeoff(plane, outpost)
	}

	private def takeoff (plane : PlaneComponent, outpost : OutpostComponent) = {
		/* happens in caller thread */
		val (dist,time) = distanceAndTime(plane, outpost)
		val src = plane.location.get

		plane.target = outpost
		plane.location = None
		val ttl = Calendar.getInstance()
		ttl.add(Calendar.SECOND, time.intValue())
		plane.timeToLand = Some(new Timestamp(ttl.getTimeInMillis))
		EntitySystem.update(plane)

		def walk (q : List[PlaneComponent]) : List[PlaneComponent] = q match {
			case Nil =>
				plane :: Nil
			case x :: rest if x.timeToLand.get.getTime > ttl.getTimeInMillis =>
				plane :: x :: rest
			case x :: rest =>
				x :: walk(rest)
		}
		/* inserts into event loop */
		synchronized {
			queue = walk(queue)
			notify()
		}
		sendMsg ! Takeoff(plane,src)
	}

	private def land (plane : PlaneComponent) = {
		/* happens in event loop */
		plane.location = plane.target
		plane.timeToLand = None
		plane.target = None
		plane.targetXY = None
		EntitySystem.update(plane)

		/* call out */
		sendMsg ! Landing(plane, plane.target.get)
	}

	private def init () = inTransaction {
		val all = from(planes) { p => where(p.targetX isNotNull) select(p) orderBy (p.timeToLand asc) }
		synchronized { queue = all.toList }
	}

	override def run () : Unit = {
		init ()
		while (true) { // TODO sane ending condition
			val plane = synchronized {
				if (queue.isEmpty) wait()
				while (Calendar.getInstance().getTimeInMillis < queue.head.timeToLand.get.getTime) {
					Thread.sleep(1000)
				}
				val head = queue.head
				queue = queue.tail
				head
			}
			land(plane)
		}
	}

	start()
}
