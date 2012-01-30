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

	def flyTo (outpost : OutpostComponent, outposts : IdMemoizeTransform) = {
		Flying.flyTo(plane, outpost)
		SetHtml("#all_locations", outposts.applyAgain())
	}

	def locations = {
		preview = false
		val outposts = memoize(idMemoize(outposts =>
		".outpost *" #> Locations.outpostsWithDistances(plane).map { case (outpost, reachable) => {
				".outpost_name" #> outpost.name &
				".options" #> { reachable match {
					case InRange(time) =>
						".opt_flythere ^^" #> "nothing" &
						".opt_flythere" #> {
							".fly [onclick]" #> ajaxInvoke(() => flyTo(outpost, outposts)) &
							".time" #> time.toString
						}
					case OutOfRange =>
						".opt_outofrange ^^" #> "nothing"
					case CurrentLocation =>
						".opt_here ^^" #> "nothing"
					case _ => info("something fishy: "+outpost.toString + " -- " + reachable.toString)
						".bla" #> "nothing"
				} } &
				".newdist" #>  { if (preview) Some(Flying.distance(coords, outpost.XY).toString) else None }
		} }))

		"#all_locations *" #> outposts &
		"#newlocation" #> newlocation(outposts)
	}

	var preview = false
	var x = ""
	var y = ""
	var name = ""
	var coords : Flying.Coords = (0.0,0.0)

	private object BadValueException extends Exception

	def dopreview (element : MemoizeTransform) = {
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
			SetHtml("all_locations", element.applyAgain())
		} catch {
			case BadValueException => Noop
		}
	}

	def docreate (element : MemoizeTransform) = {
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
				SetHtml("all_locations", element.applyAgain())
			}
		} catch {
			case BadValueException =>
				S.error("X and Y must be numbers")
				Noop
		}
	}

	def newlocation (element : MemoizeTransform) =
		"#newname" #> text (name, name = _) &
		"#newX" #> text (x, x = _) &
		"#newY" #> text (y, y = _) &
		"name=preview" #> ajaxSubmit ("preview", () => dopreview(element)) &
		"name=create" #> ajaxSubmit ("create", () => docreate(element))
}
