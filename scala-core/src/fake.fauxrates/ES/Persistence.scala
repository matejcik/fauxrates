package fake.fauxrates.ES

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import adapters.PostgreSqlAdapter
import collection.immutable.HashMap


class PersistentEntity extends KeyedEntity[Persistence.KeyType] {
	val id : Persistence.KeyType = -1
	val comment : String = ""
}

object Persistence extends Schema {
	type KeyType = Long

	private val driver = "org.postgresql.Driver"
	private val connection = "jdbc:postgresql://localhost/fauxrates"
	private val username = "fauxrates"
	private val password = "aaa"

	Class.forName(driver)
	SessionFactory.concreteFactory = Some{() =>
		Session.create(
			java.sql.DriverManager.getConnection(connection, username, password),
		    new PostgreSqlAdapter
		)
	}

	def createSchema = transaction { this.create }

	val entities = table[PersistentEntity]("entities")

	def transaction[T] (a : => T) : T = inTransaction(a)

	var tables = new HashMap[Manifest[_], Table[_]]

	def register[A <: Component] () (implicit m : Manifest[A]) : Unit = synchronized {
		if (tables contains m) throw new Exception("we already have "+m+"!")
		val t = table[A]
		on(t) { item => declare( item.id is primaryKey ) }
		tables += m -> t
	}

	def tableFor[A <: Component] () (implicit m : Manifest[A]) = tables(m).asInstanceOf[Table[A]]
}