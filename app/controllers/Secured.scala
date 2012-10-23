package controllers

import play.api.mvc._
import play.api.data._
import models.User

trait Secured extends Controller {

	def AuthenticatedAction (f : Secured.Context => Result) : Action[AnyContent] =
		Action { request =>
			val username = validUser(request.session)
			val logoutToken = request.session.get("logoutToken")
			if (username.isDefined && logoutToken.isDefined)
				f(Secured.Context(request, User.find(username.get), logoutToken.get))
			else
				Unauthorized(views.html.login(Login.loginForm)).withNewSession
		}

	def validUser (session: Session) = {
		// TODO
		session.get("username")
	}
}

object Secured {
	case class Context (request : Request[AnyContent], user: User, logoutToken: String)
}