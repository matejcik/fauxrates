package controllers

import models.Outpost
import fake.fauxrates.flying.{Flying, OutpostComponent}

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._

abstract class PlanePosition
case class PlaneSitting(outpost: OutpostComponent) extends PlanePosition
case class PlaneFlying(to: OutpostComponent, time: Int, x: Double, y: Double) extends PlanePosition

case class NewOutpost (name: String, x: Int, y: Int)

object WorldMap extends Controller with Secured {

	val newOutpostForm = Form (mapping (
		"name" -> nonEmptyText,
		"x" -> of[Int],
		"y" -> of[Int]
	)(NewOutpost.apply)(NewOutpost.unapply))

	def index = AuthenticatedAction { context =>
		val plane = context.user.findPlane
		val position = context.user.inFlight.map { i =>
			val (_, time) = Flying.distanceAndTime(plane, i.target.get)
			val (x, y) = i.XY
			PlaneFlying(i.target.get, time, x, y)
		}.getOrElse(PlaneSitting(plane.location.get))

		Ok(views.html.worldmap.index(Outpost.withRanges(plane), position, context))
	}

	def outpostDetails (id: Long) = TODO

	def createOutpost = AuthenticatedAction { context =>
		Ok(views.html.worldmap.create(newOutpostForm, context))
	}

	def createOutpostSubmit = AuthenticatedAction { context =>
		newOutpostForm.bindFromRequest()(context.request).fold(
			formWithErrors => BadRequest(views.html.worldmap.create(formWithErrors, context)),
			newOutpost => Redirect(routes.WorldMap.index())
		)
	}
}
