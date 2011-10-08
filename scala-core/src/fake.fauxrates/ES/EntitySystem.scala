package fake.fauxrates.ES

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode.{transaction => _, _}
import Persistence._

abstract class Component extends KeyedEntity[EntitySystem.Entity] {
	var id : EntitySystem.Entity = -1
}

object EntitySystem {

	type Entity = Persistence.KeyType

	def createEntity () = {
		val entity = new PersistentEntity()
		transaction { entities insert entity }
		entity.id
	}

	def deleteEntity (n : Entity) : Unit = {
		transaction {
			entities delete n
		}
	}

	def update[A <: Component] (entity : Entity, component : A) (implicit m : Manifest[A]) : Unit = {
		if (component.id >= 0 && entity != component.id)
			throw new Exception("this component already has a perfectly good entity")
		component.id = entity
		transaction { tableFor[A] insertOrUpdate component }
	}

	def get[A <: Component](entity: Entity) (implicit m : Manifest[A]) : Option[A] =
		transaction { tableFor[A] lookup entity }

	def has[A <: Component](entity: Entity) (implicit m : Manifest[A]) =
		get[A](entity) isDefined

	def remove[A <: Component](entity : Entity) (implicit m : Manifest[A]) : Unit =
		transaction { tableFor[A] delete entity }

	def remove[A <: Component](component : A) (implicit m : Manifest[A]) : Unit =
		transaction { tableFor[A] delete component.id }

	def entityFor[A <: Component] (component : A) =
		if (component.id >= 0) Some(component.id) else None

	def allOf[A <: Component] () (implicit m : Manifest[A]) : Iterable[A] = {
		transaction { from(tableFor[A]) { select(_) } map {(x)=>x} }
	}
}