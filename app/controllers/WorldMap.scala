package controllers

import play.api.mvc._
import models.Outpost

object WorldMap extends Controller with Secured {

	def index = AuthenticatedAction { context =>
		val plane = context.user.findPlane
		Ok(views.html.worldmap(Outpost.withRanges(plane), context))
	}

	def outpostDetails (id: Long) = TODO

	def createOutpost = TODO
}
