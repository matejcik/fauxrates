package fake.fauxrates
package liftweb
package comet

import model._
import flying._

import net.liftweb.http.CometActor

class Traffic extends EntityCometListener {
	def render = null

	val entity = User.currentUser.get.id

}