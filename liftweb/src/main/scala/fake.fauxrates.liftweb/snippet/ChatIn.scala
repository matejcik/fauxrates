package fake.fauxrates.liftweb
package snippet

import model._
import net.liftweb.http._
import js.JsCmds.SetValById
import net.liftweb.common.Logger

object ChatIn extends Logger {
	def render = SHtml.onSubmit( s => {
		if (User.isLoggedIn) {
			info("message from "+User.currentUser.get.name)
			ChatModel ! (User.currentUser.get.character, s)
		} else {
			warn("no current user!")
		}
		SetValById("chat_in", "")
	})
}
