package controllers

import play.api.mvc.{Security, Action, Controller}
import play.api.data.Form
import play.api.data.Forms._

object Login extends Controller {

	type LoginForm = Form[String]

	val loginForm : LoginForm = Form(
		"nickname" -> nonEmptyText(minLength = 3)
	)

	def login = Action {
		Ok(views.html.login(loginForm))
	}

	def loginSubmit = Action { implicit request =>
		loginForm.bindFromRequest.fold(
			formWithErrors => BadRequest(views.html.login(formWithErrors)),
			user => Redirect(routes.Main.index).withSession(
				"username" -> user,
				"logoutToken" -> java.util.UUID.randomUUID().toString
			)
		)
	}

	def logout (token: String) = Action { implicit request =>
		session.get("logoutToken").map { sToken =>
			if (sToken.equals(token))
				Redirect(routes.Main.index).withNewSession
			else
				Redirect(routes.Main.index)
		}.getOrElse {
			Redirect(routes.Login.login)
		}
	}

}
