package fake.fauxrates.liftweb
package comet

import model._
import collection.immutable.Queue

import net.liftweb._
import common.Logger
import http._
import util._

class Chat extends CometActor with CometListener with Logger {
	private var messages : Queue[ChatModel.Message] = Queue()

	protected def registerWith = ChatModel


	override def lowPriority = {
		case m : Queue[ChatModel.Message] =>
			info("updating messages")
			messages = m
			reRender()
	}

	def render = ".repeat *" #> {
		messages map { case (char, msg) => ".nick *" #> char.name & ".message *" #> msg }
	}
}