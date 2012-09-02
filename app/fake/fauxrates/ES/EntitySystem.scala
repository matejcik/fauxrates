package fake.fauxrates.ES

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._

import Persistence._

abstract class Component extends KeyedEntity[EntitySystem.Entity] {
	var id: EntitySystem.Entity = -1
}

object EntitySystem {

	type Entity = Persistence.KeyType

	private def insertEntity(entity: PersistentEntity) = {
		inTransaction {
			entities insert entity
		}
		entity.id
	}

	def createEntity = insertEntity(PersistentEntity.nameless)

	def createNamed(name: String, comment: String) = insertEntity(PersistentEntity.named(name, comment))

	def findNamed(name: String) = inTransaction {
		val query = entities.where(e => e.name === Some(name))
		if (query.nonEmpty) Some(query.single.id)
		else None
	}

	def deleteEntity(n: Entity) {
		inTransaction {
			entities delete n
		}
	}

	def add[A <: Component](entity: Entity, component: A)(implicit m: Manifest[A]) {
		if (component.id >= 0 && entity != component.id)
			throw new Exception("this component already has a perfectly good entity")
		component.id = entity
		inTransaction {
			tableFor[A] insertOrUpdate component
		}
	}

	def update[A <: Component](component: A)(implicit m: Manifest[A]) {
		if (component.id == -1) throw new Exception("you first need to find an entity")
		else add(component.id, component)
	}

	def get[A <: Component](entity: Entity)(implicit m: Manifest[A]): Option[A] =
		inTransaction {
			tableFor[A] lookup entity
		}

	def has[A <: Component](entity: Entity)(implicit m: Manifest[A]) =
		get[A](entity).isDefined

	def remove[A <: Component](entity: Entity)(implicit m: Manifest[A]) {
		inTransaction {
			tableFor[A] delete entity
		}
	}

	def remove[A <: Component](component: A)(implicit m: Manifest[A]) {
		inTransaction {
			tableFor[A] delete component.id
		}
	}

	def entityFor[A <: Component](component: A) =
		if (component.id >= 0) Some(component.id) else None

	def allOf[A <: Component]()(implicit m: Manifest[A]): Iterable[A] = {
		inTransaction {
			from(tableFor[A]) {
				select(_)
			} map {
				(x) => x
			}
		}
	}
}
