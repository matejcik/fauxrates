import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

	val appName         = "Fauxrates"
	val appVersion      = "1.0-SNAPSHOT"

	val appDependencies = Seq(
		// Add your project dependencies here,
		"org.squeryl" %% "squeryl" % "0.9.5-2",
		//"com.h2database" % "h2" % "1.2.127",
		"postgresql" % "postgresql" % "9.1-901.jdbc4"
	)

	val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
		// Add your own project settings here
	)

}
