package views.html

import views.html.helper.{FieldElements, FieldConstructor}
import play.api.data.{FormError, Field}
import play.api.i18n.{Messages, Lang}
import org.springframework.validation.FieldError

package object bootstrap {
	implicit val bootstrapField = new FieldConstructor {
		def apply(elts: FieldElements) = fields(elts)
	}

	def errorsFor (errors : Field*)(implicit lang: Lang) = {
		val hasErrors = errors exists (_.hasErrors)
		//val errorFields = (Seq[FormError]() /: errors) { (a,b) => a ++ b.errors }
		val errorString = errors.foldLeft(Seq[FormError]()){ (a,b) => a ++ b.errors }.map {
			e => Messages(e.message, e.args: _*)
		}.mkString(", ")

		(hasErrors, errorString)
	}
}