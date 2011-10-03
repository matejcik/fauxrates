package fake.fauxrates.ES

import collection.immutable.{HashSet => ImmutableHashSet, HashMap => ImmutableHashMap}
import collection.JavaConversions._
import collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap

abstract class Component

object EntitySystem {

	type Entity = Long
	private type Store[C <: Component] = ConcurrentMap[Entity, C]

	var componentStores = new ImmutableHashMap[Manifest[_ <: Component], Store[_]]

	var allEntities = new ImmutableHashSet[Entity]

	private var lastEntity : Entity = 0

	def createEntity () = synchronized {
		val n = lastEntity
		lastEntity += 1
		allEntities += n
		n
	}

	def deleteEntity (n : Entity) = synchronized {
		allEntities -= n
	}

	def addComponent[A <: Component] (entity : Entity, component : A) (implicit m : Manifest[A]) : Unit = {
		var ismap = componentStores get m
		if (ismap.isEmpty) synchronized {
			ismap = componentStores get m
			if (ismap.isEmpty) /* still */ {
				ismap = Some(new ConcurrentHashMap[Entity, A])
				componentStores += m -> ismap.get
			}
		}
		val store = ismap.get.asInstanceOf[Store[A]]
		store += entity -> component
	}

	def get[A <: Component](entity: Entity) (implicit m : Manifest[A]) = componentStores get m match {
		case Some(store : Store[_]) => store.asInstanceOf[Store[A]] get entity
		case _ => None
	}

	def has[A <: Component](entity: Entity) (implicit store : Manifest[A]) =
		componentStores get store map { _ contains entity } getOrElse false

	def remove[A <: Component](entity : Entity) (implicit store : Manifest[A]) : Unit =
		componentStores get store map { _ remove entity }

	def entityFor[A <: Component] (component : A) (implicit m : Manifest[A]) = componentStores get m match {
		case Some(store : Store[_]) => store.asInstanceOf[Store[A]] find {_._2 == component} map {_._1}
		case _ => None
	}

	//def remove[A <: Component] (component : A) : Unit = remove(component.entity, classof(component))
}