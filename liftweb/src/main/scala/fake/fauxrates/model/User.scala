package fake.fauxrates.model

import net.liftweb.common._
import fake.fauxrates.ES._
import net.liftweb.http.SessionVar
import java.util.Calendar

class User (val name : String) {
	val component = new LiftwebLoginComponent(this)
	val loggedIn = Calendar.getInstance().getTime

	/* TODO this should maybe destroy the session as well? */
	def logout () = EntitySystem.entityFor(component) map {EntitySystem.remove[LiftwebLoginComponent](_)}
}

class LiftwebLoginComponent (val user : User) extends Component

object User {
	private object curUser extends SessionVar[Box[User]](Empty) {
		registerCleanupFunc(_ => cleanup)

		private def cleanup () : Unit = this.is.map {_.logout()}
	}

	def login (name : String) = {
		logout()
		val u = new User(name)
		EntitySystem.addComponent(EntitySystem.createEntity(), u.component)
		curUser.set(Full(u))
	}

	def logout () = {
		for (user <- curUser) user.logout()
		curUser.remove()
	}

	def isLoggedIn () = curUser.isDefined
	def currentUser = curUser.get

	def onlineUsers = EntitySystem.allOf[LiftwebLoginComponent] map {_.user}
}