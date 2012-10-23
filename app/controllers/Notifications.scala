package controllers

import play.api.mvc.{WebSocket, Controller}
import play.api.libs.iteratee._
import fake.fauxrates.flying.Flying.{Landing, Takeoff}
import fake.fauxrates.flying.{Flying, OutpostComponent, PlaneComponent}
import fake.fauxrates.ES.EntitySystem
import models.CharacterComponent
import collection.immutable.HashSet

object Notifications extends Controller with Secured {

	def traffic = WebSocket.using[String] { request =>
		if (validUser(request.session).isDefined)
			connectOk
		else
			connectFail
	}

	private def connectFail = {
		val in = Done[String, Unit]((), Input.EOF)
		val out = Enumerator[String]("unauthorized, methinks") >>> Enumerator.eof
		(in, out)
	}

	private def connectOk = {
		val out = Enumerator.imperative[String]()
		connections += out
		val in = Iteratee.consume[String]().mapDone { _ => connections -= out }
		(in, out)
	}

	var connections: Set[PushEnumerator[String]] = HashSet()

	private def makeMessage(plane: PlaneComponent, outpost: OutpostComponent, between: String) = {
		val charName = EntitySystem.get[CharacterComponent](plane.id)
			.map { c => c.name }
			.getOrElse("a rogue plane")
		charName + " " + between + " " + outpost.name
	}

	def sendmsg (msg: String) {
		connections map (_.push(msg))
	}

	def receive (what: Any) = {
		what match {
			case Takeoff(plane, from) =>
				sendmsg(makeMessage(plane, from, "has just taken off from"))
			case Landing(plane, on) =>
				sendmsg(makeMessage(plane, on, "has just landed at"))
		}
	}

	Flying.subscribe(receive)
}