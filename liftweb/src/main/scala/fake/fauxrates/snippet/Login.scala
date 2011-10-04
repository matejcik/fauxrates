package fake.fauxrates.snippet

import xml.NodeSeq
import net.liftweb.util.Helpers._
import fake.fauxrates.model.User
import net.liftweb.http.{S, SHtml}

class Login {

	def login (xhtml : NodeSeq) : NodeSeq = {
		var name = if (User.isLoggedIn()) User.currentUser.open_!.name else "ferret";

		def dologin () = {
			User.login(name)
			S.redirectTo("/")
		}

		bind("entry", xhtml,
		    "username" -> SHtml.text(name, name = _),
			"submit" -> SHtml.submit("Login", dologin)
		)
	}
}