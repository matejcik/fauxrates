package fake.fauxrates

import org.specs2.mutable._

class EntitySystemTest extends Specification {

	args(sequential = true)

	var entity : EntitySystem.Entity = 0
	var en : EntitySystem.Entity = 0
	var em : EntitySystem.Entity = 0

	class MockComponent (val i : Int) extends Component
	class BarComponent (val s : String) extends Component
	class FreakComponent extends Component
	class MissingComponent extends Component

	val enm = new MockComponent(9)
	val enb = new BarComponent("baz")
	val emm = new MockComponent(13)
	val emb = new BarComponent("foo")
	val enf = new FreakComponent

	"Entity System" should {
		"create new entities" in {
			entity = EntitySystem.createEntity
			en = EntitySystem.createEntity
			em = EntitySystem.createEntity
			success
		}

		"delete existing and nonexistent entities" in {
			EntitySystem.deleteEntity(3099)
			EntitySystem.deleteEntity(entity)
			success
		}

		"add components to entities" in {
			EntitySystem.addComponent(en, enm)
			EntitySystem.addComponent(en, enb)
			EntitySystem.addComponent(en, enf)

			EntitySystem.addComponent(em, emm)
			EntitySystem.addComponent(em, emb)
			success
		}

		"check if components are on entities" in {
			EntitySystem.hasComponent(em, classOf[MockComponent]) must beTrue
			EntitySystem.hasComponent(em, classOf[BarComponent]) must beTrue
			EntitySystem.hasComponent(em, classOf[FreakComponent]) must beFalse

			EntitySystem.hasComponent(en, classOf[BarComponent]) must beTrue
			EntitySystem.hasComponent(en, classOf[FreakComponent]) must beTrue
			EntitySystem.hasComponent(en, classOf[MissingComponent]) must beFalse

			EntitySystem.hasComponent(entity, classOf[MockComponent]) must beFalse
		}

		"retrieve said components" in {
			val mc = EntitySystem.getComponent(em, classOf[MockComponent])
			mc must beSome
			mc.get mustEqual emm
			val nc = EntitySystem.getComponent(en, classOf[MockComponent])
			nc must beSome
			nc.get mustEqual enm

			EntitySystem.getComponent(em, classOf[FreakComponent]) must beNone
			EntitySystem.getComponent(entity, classOf[MockComponent]) must beNone
		}

		"and remove them" in {
			EntitySystem.removeComponent(em, classOf[MockComponent])
			EntitySystem.getComponent(em, classOf[MockComponent]) must beNone
			EntitySystem.getComponent(en, classOf[MockComponent]) must beSome

			EntitySystem.removeComponent(em, classOf[MockComponent])
			success
		}
	}
}