package fake.fauxrates.ES

import javax.persistence._
import scala.reflect._

@Entity @Table(name = "entities")
class PersistentEntity {

	@Id
	@SequenceGenerator(name = "entity_generator", sequenceName = "entities_id_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="entity_generator")
	@BeanProperty var id : Persistence.KeyType = _

	@BeanProperty var comment : String = _
}

object Persistence {
	type KeyType = Long

	private val factory = javax.persistence.Persistence.createEntityManagerFactory("fauxrates-persistence")

	def session () = factory.createEntityManager()
}