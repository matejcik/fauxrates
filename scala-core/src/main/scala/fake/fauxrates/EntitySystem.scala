package fake.fauxrates

import collection.immutable.{HashSet => ImmutableHashSet, HashMap => ImmutableHashMap}
import collection.JavaConversions._
import collection.mutable.ConcurrentMap
import java.util.concurrent.ConcurrentHashMap

abstract class Component {}

object EntitySystem {

	type Entity = Long
	private type Store[C <: Component] = ConcurrentMap[Entity, C]

	var componentStores = new ImmutableHashMap[Class[_ <: Component], Store[_]]

	var allEntities = new ImmutableHashSet[Entity]

	private var lastEntity : Entity = 0

	def createEntity = synchronized {
		val n = lastEntity
		lastEntity += 1
		allEntities += n
		n
	}

	def deleteEntity (n : Entity) = synchronized {
		allEntities -= n
	}

	private def classof[A <: AnyRef] (item : A) : Class[A] = item.getClass.asInstanceOf[Class[A]]

	def addComponent[A <: Component] (entity : Entity, component : A) : Unit = {
		val c = classof(component)
		println("adding to : "+entity+" ("+component+")")
		var ismap = componentStores get c
		if (ismap.isEmpty) synchronized {
			ismap = componentStores get c
			if (ismap.isEmpty) /* still */ {
				ismap = Some(new ConcurrentHashMap[Entity, A])
				componentStores += c -> ismap.get
			}
		}
		val store = ismap.get.asInstanceOf[Store[A]]
		store += entity -> component
	}

	def getComponent[A <: Component] (entity : Entity, klass : Class[A]) =
		componentStores get klass match {
			case Some(store : Store[_]) => store.asInstanceOf[Store[A]] get entity
			case _ => None
		}

	def hasComponent[A <: Component] (entity : Entity, klass : Class[A]) =
		componentStores get klass match {
			case Some(store : Store[_]) => store.asInstanceOf[Store[A]] contains entity
			case _ => false
		}

	def removeComponent[A <: Component] (entity : Entity, klass : Class[A]) : Unit =
		componentStores get klass match {
			case Some(store : Store[_]) => store.asInstanceOf[Store[A]] -= entity
			case _ =>
		}
}