package fake.fauxrates.liftweb.snippet

import xml.NodeSeq
import net.liftweb.util.Helpers._
import fake.fauxrates.liftweb.model.User
import net.liftweb.http.{S, SHtml}

class Login {

	def login (xhtml : NodeSeq) : NodeSeq = {
		var name = if (User.isLoggedIn()) User.currentUser.open_!.name else "ferret";

		def dologin () = {
			if (User.login(name)) S.redirectTo("/")
			else S.error("invalid user name or password or whatever")
		}

		bind("entry", xhtml,
		    "username" -> SHtml.text(name, name = _),
			"submit" -> SHtml.submit("Login", dologin)
		)
	}
}
