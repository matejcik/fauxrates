package fake.fauxrates.liftweb.model

import net.liftweb.common._
import fake.fauxrates.ES._
import net.liftweb.http.SessionVar
import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import org.squeryl.PrimitiveTypeMode._

class User (val name : String) extends KeyedEntity[Long] {
	var id : Long = -1
	@Transient var character : CharacterComponent = _
}

class CharacterComponent (val player : Long, val name : String) extends Component {
	var online = false
}

object User {

	val users = Persistence.registerTable[User]("users")
	val characters = Persistence.tableFor[CharacterComponent]

	private object curUser extends SessionVar[Box[User]](Empty) {
		registerCleanupFunc(_ => cleanup)

		private def cleanup () : Unit = logout
	}

	def login (name : String) = {
		logout()

		/* create/find a user record */
		val user = transaction {
			val res = users.where(u => u.name === name)
			if (res.isEmpty) {
				val u = new User(name)
				users insert u
				u
			} else res.head
		}
		curUser.set(Full(user))

		/* create/find a character */
		user.character = transaction {
			val chars = characters.where(c => c.player === user.id)
			if (chars.isEmpty) {
				// generate a name
				val cname = if (name.split(" ").size > 1) name
							else name + " " + surnames(util.Random.nextInt(surnames.size))
				val entity = EntitySystem.createEntity
				val component = new CharacterComponent(user.id, cname)
				component.id = entity
				component
			} else chars.head
		}
		user.character.online = true
		EntitySystem update user.character

		/* and done */
		true
	}

	val surnames = List("Skywalker", "Cloudkicker", "Rincewind", "Kitesfear", "Stratocaster")

	def logout () = {
		for (user <- curUser) transaction { user.character.online = false; EntitySystem update user.character }
		curUser.remove()
	}

	def isLoggedIn () = curUser.isDefined
	def currentUser = curUser.get

	def onlineUsers = transaction {
		from(characters) { user => where(user.online === true) select(user) } map {(x) => x}
	}
}
