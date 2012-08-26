package controllers

import play.api._
import play.api.mvc._

object Main extends Controller with Secured {

	def index = AuthenticatedAction { context =>
		Ok(views.html.index(context))
	}
}