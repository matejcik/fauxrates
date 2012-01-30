package fake.fauxrates
package liftweb
package comet

import model._
import flying._

import net.liftweb.http.CometActor
import xml.NodeSeq

class Traffic extends EntityCometListener {

	def render =
		"#sitting" #> NodeSeq.Empty &
	    "#enroute" #> NodeSeq.Empty

	val entity = User.currentUser.get.id

}
