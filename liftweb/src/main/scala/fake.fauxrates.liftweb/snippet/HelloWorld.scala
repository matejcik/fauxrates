package fake.fauxrates.liftweb {
package snippet {

import _root_.scala.xml.NodeSeq
import _root_.net.liftweb.util.Helpers
import Helpers._
import model.User

class HelloWorld {
	def howdy(in: NodeSeq): NodeSeq =
		Helpers.bind("b", in,
			"time" -> (new _root_.java.util.Date).toString,
			"user" -> User.currentUser.map{_.name}.getOrElse("you're not supposed to be here!")
		)

	def users (in : NodeSeq) : NodeSeq =
		User.onlineUsers flatMap {
			user => bind("b", in, "username" -> user.name, "since" -> user.loggedIn.toString)
		} toSeq
}

}

}
