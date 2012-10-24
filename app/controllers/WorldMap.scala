package controllers

import models.Outpost
import fake.fauxrates.flying.{Flying, OutpostComponent}

import play.api.mvc._
import play.api.data.{FormError, Form}
import play.api.data.Forms._
import play.api.data.format.Formats._
import fake.fauxrates.ES.EntitySystem

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
		val position = plane.inFlight.map { i =>
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
			newOutpost => try {
				Outpost.create(newOutpost.name, newOutpost.x, newOutpost.y)
				Redirect(routes.WorldMap.index())
			} catch {
				case Outpost.DuplicateException => {
					val form = newOutpostForm.fill(newOutpost)
						.copy[NewOutpost](
							errors = Seq(FormError("name", "duplicate")),
							value = Some(newOutpost))
					BadRequest(views.html.worldmap.create(form, context))
				}
			}
		)
	}

	def fly(id: Long) = AuthenticatedAction { context =>
		try {
			EntitySystem.get[OutpostComponent](id).map { outpost =>
				Flying.flyTo(context.user.findPlane, outpost)
			}
		} catch { case _ => /* yada yada */ }
		Redirect(routes.WorldMap.index())
	}
}
