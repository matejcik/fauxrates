package fake.fauxrates.liftweb
package snippet

import model._
import net.liftweb.http._
import js.JsCmds.SetValById

object ChatIn {
	def render = SHtml.onSubmit( s => {
		if (User.isLoggedIn) ChatModel ! (User.currentUser.get.character, s)
		SetValById("chat_in", "")
	})
}
