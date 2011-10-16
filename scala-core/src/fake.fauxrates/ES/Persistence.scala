package fake.fauxrates.ES

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import adapters.PostgreSqlAdapter
import collection.immutable.HashMap
import internals.FieldMetaData


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
		    new PostgreSqlAdapter {
			    override def createSequenceName (fmd : FieldMetaData) =
			        fmd.parentMetaData.viewOrTable.name + "_" + fmd.columnName + "_seq"
		    }
		)
	}

	private val camelre = "([a-z])([A-Z])".r
	private def removeCamel (s : String) = camelre.replaceAllIn(s, m => m.group(1) + "_" + m.group(2)).toLowerCase

	override def tableNameFromClassName (n : String) = removeCamel(n)
	override def tableNameFromClass (c : Class[_]) = tableNameFromClassName(c.getSimpleName)
	override def columnNameFromPropertyName (n : String) = removeCamel(n)

	def createSchema = inTransaction { this.create }

	val entities = table[PersistentEntity]("entities")

	var tables = new HashMap[Manifest[_], Table[_]]

	def register[A <: Component] () (implicit m : Manifest[A]) = register[A](tableNameFromClass(m.erasure))

	def register[A <: Component] (name : String) (implicit m : Manifest[A]) = synchronized {
		if (tables contains m) tables(m)
		else {
			val t = table[A](name)
			on(t) { item => declare( item.id is primaryKey ) }
			tables += m -> t
			t
		}
	}.asInstanceOf[Table[A]]

	def registerTable[A] (name : String = null) (implicit m : Manifest[A]) =
		table[A](if (name == null) tableNameFromClass(m.erasure) else name)

	def tableFor[A <: Component] () (implicit m : Manifest[A]) : Table[A] =
		if (tables contains m) tables(m).asInstanceOf[Table[A]]
		else register[A]
}
