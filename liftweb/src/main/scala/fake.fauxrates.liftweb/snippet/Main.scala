package fake.fauxrates
package liftweb
package snippet

import ES._
import flying._
import model._
import model.Locations._
import net.liftweb._
import common.{Logger, Empty, Full}
import http._
import http.js.JsCmd
import http.js.JsCmds._
import http.SHtml._
import util.Helpers._
import xml.NodeSeq

class Main extends Logger {

	val plane = {
			val id = User.currentUser.get.character.id
			val plane = EntitySystem.get[PlaneComponent](id)
			if (plane.isDefined) plane.get
			else {
				val zeroone = EntitySystem.get[OutpostComponent](EntitySystem.findNamed("OUTPOST_ZERO").get).get
				val plane = new PlaneComponent(zeroone)
				EntitySystem.add(id, plane)
				plane
			}
		}

	val all_locations = {
		val tmpl = Templates(List("main")).open_!
		tmpl \\ "div" filter { _ \\ "@id" exists { _.text == "all_locations" } }
	}

	def rerender () : JsCmd = {
		SetHtml("all_locations", locations(all_locations))
	}

	def flyTo (outpost : OutpostComponent) = {
		Flying.flyTo(plane, outpost)
		rerender()
	}

	def locations = {
		info("called locations")
		".outpost *" #> Locations.outpostsWithDistances(plane).map { case (outpost, reachable) => {
			var t =
				".outpost_name" #> outpost.name &
				".options" #> { reachable match {
					case InRange(time) =>
						".opt_flythere ^^" #> "nothing" &
						".opt_flythere" #> {
							".fly [onclick]" #> ajaxInvoke(() => flyTo(outpost)) &
							".time" #> time.toString
						}
					case OutOfRange =>
						".opt_outofrange ^^" #> "nothing"
					case CurrentLocation =>
						".opt_here ^^" #> "nothing"
					case _ => info("something fishy: "+outpost.toString + " -- " + reachable.toString)
						".bla" #> "nothing"
				} }
			if (preview) t &= ".newdist" #> Flying.distance (coords, outpost.XY).toString
			else t &= ".newout" #> NodeSeq.Empty
			// HOW THE FUCK CAN THIS BE DONE INLINE
			t
		} }
	}

	var preview = false
	var x = ""
	var y = ""
	var name = ""
	var coords : Flying.Coords = (0.0,0.0)

	private object BadValueException extends Exception

	def dopreview () = {
		try {
			val xx = asDouble(x) match {
				case Full(x) => x
				case _ => throw BadValueException
			}
			val yy = asDouble(y) match {
				case Full(y) => y
				case _ => throw BadValueException
			}
			coords = (xx, yy)
			preview = true
			rerender()
		} catch {
			case BadValueException => Noop
		}
	}

	def docreate () = {
		try {
			val xx = asDouble(x) match {
				case Full(x) => x
				case _ => throw BadValueException
			}
			val yy = asDouble(y) match {
				case Full(y) => y
				case _ => throw BadValueException
			}
			if (name.isEmpty) {
				S.error("name must not be empty")
				Noop
			} else {
				preview = false
				val outpost = new OutpostComponent(name, xx, yy)
				EntitySystem.add(EntitySystem.createEntity(), outpost)
				rerender()
			}
		} catch {
			case BadValueException =>
				S.error("X and Y must be numbers")
				Noop
		}
	}

	def newlocation =
		"#newname" #> text (name, name = _) &
		"#newX" #> text (x, x = _) &
		"#newY" #> text (y, y = _) &
		"name=preview" #> ajaxSubmit ("preview", dopreview) &
		"name=create" #> ajaxSubmit ("create", docreate)
}
