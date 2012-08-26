/* from https://github.com/jamesward/play2bars/tree/scala-squeryl */

import org.squeryl.adapters.{H2Adapter, PostgreSqlAdapter}
import org.squeryl.internals.{FieldMetaData, DatabaseAdapter}
import org.squeryl.{Session, SessionFactory}
import play.api.db.DB
import play.api.GlobalSettings

import play.api.Application

trait SensibleSequenceName extends DatabaseAdapter {
	override def createSequenceName(fmd: FieldMetaData) =
		fmd.parentMetaData.viewOrTable.name + "_" + fmd.columnName + "_seq"
}

object Global extends GlobalSettings {

	override def onStart(app: Application) {
		SessionFactory.concreteFactory = app.configuration.getString("db.default.driver") match {
			case Some("org.h2.Driver") =>
				Some(() => getSession(new H2Adapter with SensibleSequenceName, app))
			case Some("org.postgresql.Driver") =>
				Some(() => getSession(new PostgreSqlAdapter with SensibleSequenceName, app))
			case _ => sys.error("Database driver must be either org.h2.Driver or org.postgresql.Driver")
		}
	}

	def getSession(adapter: DatabaseAdapter, app: Application) = Session.create(DB.getConnection()(app), adapter)

}