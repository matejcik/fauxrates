package fake.fauxrates.ES

import org.specs2.mutable._

case class MockComponent (val i : Int) extends Component
case class BarComponent (val s : String) extends Component
case class FreakComponent extends Component
case class MissingComponent extends Component

class EntitySystemSpec extends Specification {

	args(sequential = true)

	var entity : EntitySystem.Entity = 0
	var en : EntitySystem.Entity = 0
	var em : EntitySystem.Entity = 0

	val enm = new MockComponent(9)
	val enb = new BarComponent("baz")
	val emm = new MockComponent(13)
	val emb = new BarComponent("foo")
	val enf = new FreakComponent

	"Entity System" should {

		"register components and create schema" in {
			Persistence.register[MockComponent]
			Persistence.register[BarComponent]
			Persistence.register[FreakComponent]
			Persistence.register[MissingComponent]
			//Persistence.createSchema
			success
		}

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
			EntitySystem.update(en, enm)
			EntitySystem.update(en, enb)
			EntitySystem.update(en, enf)

			EntitySystem.update(em, emm)
			EntitySystem.update(em, emb)
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

		"find entities by components" in {
			var ef = EntitySystem.entityFor(emm)
			ef must beSome
			ef.get mustEqual em
			ef = EntitySystem.entityFor(enm)
			ef must beSome
			ef.get mustEqual en

			EntitySystem.entityFor(new FreakComponent) must beNone
		}

		"find all components of type" in {
			EntitySystem.allOf[MockComponent] must contain(enm, emm)
			EntitySystem.allOf[FreakComponent] must contain(enf).only
			EntitySystem.allOf[MissingComponent] must be empty
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
