package fake.fauxrates.ES

import org.squeryl.PrimitiveTypeMode._
import org.squeryl._
import adapters.PostgreSqlAdapter
import collection.immutable.HashMap
import internals.FieldMetaData


class PersistentEntity(val id: Persistence.KeyType, val name: Option[String], val comment: String)
	extends KeyedEntity[Persistence.KeyType] {
	def this() = this(-1, Some(""), "")
}

object PersistentEntity {
	def nameless = new PersistentEntity(-1, None, "")

	def named(name: String, comment: String = "") = new PersistentEntity(-1, Some(name), comment)
}

object Persistence extends Schema {
	type KeyType = Long

	private val camelre = "([a-z])([A-Z])".r

	private def removeCamel(s: String) = camelre.replaceAllIn(s, m => m.group(1) + "_" + m.group(2)).toLowerCase

	override def tableNameFromClassName(n: String) = removeCamel(n)

	override def tableNameFromClass(c: Class[_]) = tableNameFromClassName(c.getSimpleName)

	override def columnNameFromPropertyName(n: String) = removeCamel(n)

	def createSchema = inTransaction {
		this.create
	}

	val entities = table[PersistentEntity]("entities")

	var tableMap = new HashMap[Manifest[_], Table[_]]

	def register[A <: Component]()(implicit m: Manifest[A]) = register[A](tableNameFromClass(m.erasure))

	def register[A <: Component](name: String)(implicit m: Manifest[A]) = synchronized {
		if (tableMap contains m) tableMap(m)
		else {
			val t = table[A](name)
			on(t) {
				item => declare(item.id is primaryKey)
			}
			tableMap += m -> t
			t
		}
	}.asInstanceOf[Table[A]]

	def registerTable[A](name: String = null)(implicit m: Manifest[A]) =
		table[A](if (name == null) tableNameFromClass(m.erasure) else name)

	def tableFor[A <: Component]()(implicit m: Manifest[A]): Table[A] =
		if (tableMap contains m) tableMap(m).asInstanceOf[Table[A]]
		else register[A]
}
