package fake.fauxrates.ES

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

import Persistence._

abstract class Component extends KeyedEntity[EntitySystem.Entity] {
	var id : EntitySystem.Entity = -1
}

object EntitySystem {

	type Entity = Persistence.KeyType

	def createEntity () = {
		val entity = new PersistentEntity()
		inTransaction { entities insert entity }
		entity.id
	}

	def deleteEntity (n : Entity) : Unit = {
		inTransaction {
			entities delete n
		}
	}

	def add[A <: Component] (entity : Entity, component : A) (implicit m : Manifest[A]) : Unit = {
		if (component.id >= 0 && entity != component.id)
			throw new Exception("this component already has a perfectly good entity")
		component.id = entity
		inTransaction { tableFor[A] insertOrUpdate component }
	}

	def update[A <: Component] (component : A) (implicit m : Manifest[A]) : Unit =
		if (component.id == -1) throw new Exception("you first need to find an entity")
		else add(component.id, component)

	def get[A <: Component](entity: Entity) (implicit m : Manifest[A]) : Option[A] =
		inTransaction { tableFor[A] lookup entity }

	def has[A <: Component](entity: Entity) (implicit m : Manifest[A]) =
		get[A](entity) isDefined

	def remove[A <: Component](entity : Entity) (implicit m : Manifest[A]) : Unit =
		inTransaction { tableFor[A] delete entity }

	def remove[A <: Component](component : A) (implicit m : Manifest[A]) : Unit =
		inTransaction { tableFor[A] delete component.id }

	def entityFor[A <: Component] (component : A) =
		if (component.id >= 0) Some(component.id) else None

	def allOf[A <: Component] () (implicit m : Manifest[A]) : Iterable[A] = {
		inTransaction { from(tableFor[A]) { select(_) } map {(x)=>x} }
	}
}
