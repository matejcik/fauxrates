package fake.fauxrates
package liftweb
package snippet

import ES._
import flying._
import model._
import model.Locations._
import net.liftweb._
import http._
import http.js.JsCmd
import http.js.JsCmds._
import http.SHtml._
import util.Helpers._

class Main {

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

	def rerender () : JsCmd = {
		val tmpl = Templates(List("main")).open_!
		val ns = tmpl \\ "div" filter { _ \\ "@id" exists { _.text == "all_locations" } }
		SetHtml("all_locations", locations(ns))
	}

	def flyTo (outpost : OutpostComponent) = {
		Flying.flyTo(plane, outpost)
		rerender()
	}

	def locations =
		".outpost *" #> Locations.outpostsWithDistances(plane).map { case (outpost, reachable) =>
			".outpost_name" #> outpost.name &
			".options" #> { reachable match {
				case InRange(time) =>
					".opt_flythere ^^" #> "nothing" &
					".opt_flythere" #> {
						".fly [onclick]" #> ajaxInvoke (() => flyTo(outpost)) &
						".time" #> time.toString
					}
				case OutOfRange =>
					".opt_outofrange ^^" #> "nothing"
				case CurrentLocation =>
					".opt_here ^^" #> "nothing"
			}}
		}
}
