package fake.fauxrates
package liftweb
package snippet

import ES._
import flying._
import model.{User, Locations}
import net.liftweb._
import common.Logger
import util.Helpers._
import xml.NodeSeq

class Main {

	def locations = {
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

		".outpost *" #> Locations.outpostsWithDistances(plane).map { case (outpost, reachable) =>
			".outpost_name" #> outpost.name &
			".opt_flythere" #> "fly here" &
			".opt_enroute" #> NodeSeq.Empty &
			".opt_here" #> NodeSeq.Empty
		}
	}
}
