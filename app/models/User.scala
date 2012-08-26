package models

import fake.fauxrates.ES._
import org.squeryl.KeyedEntity
import org.squeryl.annotations._
import org.squeryl.PrimitiveTypeMode._
import fake.fauxrates.flying.{OutpostComponent, PlaneComponent}

class User (val name : String) extends KeyedEntity[Long] {
	var id : Long = -1
	var admin = false
	@Transient var character : CharacterComponent = _

	def findPlane = {
		val plane = EntitySystem.get[PlaneComponent](character.id)
		if (plane.isDefined) plane.get
		else {
			val zero = EntitySystem.get[OutpostComponent](EntitySystem.findNamed("OUTPOST_ZERO").get).get
			val plane = new PlaneComponent(zero)
			EntitySystem.add(character.id, plane)
			plane
		}
	}
}

class CharacterComponent (val player : Long, val name : String) extends Component {
	var online = false
}

object User {

	val users = Persistence.registerTable[User]("users")
	val characters = Persistence.tableFor[CharacterComponent]

	def find(name: String) = login(name)

	def login(name: String) = {
		/* create/find a user record */
		val user = transaction {
			val res = users.where(u => u.name === name)
			if (res.isEmpty) {
				val u = new User(name)
				users insert u
				u
			} else res.head
		}

		/* create/find a character */
		user.character = transaction {
			val chars = characters.where(c => c.player === user.id)
			if (chars.isEmpty) {
				// generate a name
				val cname =
					if (name.split(" ").size > 1) name
					else name + " " + surnames(util.Random.nextInt(surnames.size))
				val entity = EntitySystem.createEntity
				val component = new CharacterComponent(user.id, cname)
				component.id = entity
				component
			} else chars.head
		}
		user.character.online = true
		EntitySystem update user.character

		user
	}

	val surnames = List("Skywalker", "Cloudkicker", "Rincewind", "Kitesfear", "Stratocaster")

	def logout(user : User) = {
		transaction {
			user.character.online = false; EntitySystem update user.character
		}
	}

	def onlineUsers = transaction {
		from(characters) {
			user => where(user.online === true) select (user)
		} map {
			(x) => x
		}
	}
}
