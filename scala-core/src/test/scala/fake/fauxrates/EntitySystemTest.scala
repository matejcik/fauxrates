package fake.fauxrates.ES

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
			EntitySystem.has[MockComponent](em) must beTrue
			EntitySystem.has[BarComponent](em) must beTrue
			EntitySystem.has[FreakComponent](em) must beFalse

			EntitySystem.has[BarComponent](en) must beTrue
			EntitySystem.has[FreakComponent](en) must beTrue
			EntitySystem.has[MissingComponent](en) must beFalse

			EntitySystem.has[MockComponent](entity) must beFalse
		}

		"retrieve said components" in {
			val mc = EntitySystem.get[MockComponent](em)
			mc must beSome
			mc.get mustEqual emm
			val nc = EntitySystem.get[MockComponent](en)
			nc must beSome
			nc.get mustEqual enm

			EntitySystem.get[FreakComponent](em) must beNone
			EntitySystem.get[MockComponent](entity) must beNone
		}

		"and remove them" in {
			EntitySystem.remove[MockComponent](em)
			EntitySystem.get[MockComponent](em) must beNone
			EntitySystem.get[MockComponent](en) must beSome

			EntitySystem.remove[MockComponent](em)
			success
		}
	}
}