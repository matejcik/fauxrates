package bootstrap.liftweb

import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._
import fake.fauxrates.liftweb.model.User

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
	def boot {
		// where to search snippet
		LiftRules.addToPackages("fake.fauxrates.liftweb")


		val isLoggedIn = If(User.isLoggedIn, RedirectResponse("/login"))

		// Build SiteMap
		val entries = List[Menu](
			Menu.i("Home") / "index" >> isLoggedIn,
			Menu.i("Login") / "login" >> Unless(User.isLoggedIn, RedirectResponse("/logout")),
			Menu.i("Logout") / "logout" >> If(User.isLoggedIn, "you have to log in if you want to log out"),
			Menu.i("Chat") / "chat" >> isLoggedIn,
			Menu.i("Main") / "main" >> isLoggedIn
		)
		LiftRules.setSiteMap(SiteMap(entries: _*))

		// custom dispatch - will it work here?
		LiftRules.dispatch.append {
			case Req("logout" :: Nil, _, _) => () => {
				User.logout()
				Full(RedirectResponse("/login"))
			}
		}
	}
}

