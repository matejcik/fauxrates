name := "fauxrates-server"

organization := "fauxrates"

version := "0.1"

scalaVersion := "2.9.1"

//seq(webSettings: _*)

//scanDirectories in Compile := Nil

resolvers ++= Seq(
  "Scala Tools Releases" at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/"
)

// if you have issues pulling dependencies from the scala-tools repositories (checksums don't match), you can disable checksums
//checksums := Nil

libraryDependencies ++= {
  val liftVersion = "2.4" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default")
}

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container",
  "org.specs2" %% "specs2" % "1.7.1" % "test",
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "org.squeryl" %% "squeryl" % "0.9.5-RC1",
  "postgresql" % "postgresql" % "8.4-701.jdbc4",
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default"
)
