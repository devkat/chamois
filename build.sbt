import com.github.sbtliquibase.SbtLiquibase

organization := "org.moscatocms"

name := "moscato"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
 
libraryDependencies ++= {
  val akkaVersion = "2.3.9"
  val sprayVersion = "1.3.3"
  val slickVersion = "3.0.0"
  Seq(
    "io.spray" %% "spray-can" % sprayVersion withSources() withJavadoc(),
    "io.spray" %% "spray-routing" % sprayVersion withSources() withJavadoc(),
    "io.spray" %% "spray-json" % "1.3.2",
    "io.spray" %% "spray-client" % sprayVersion,
    "io.spray" %% "spray-testkit" % sprayVersion % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe.slick" %% "slick" % slickVersion,
    "com.typesafe.slick" %% "slick-codegen" % slickVersion,
    "org.apache.shiro" % "shiro-core" % "1.2.3",
    "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
    "org.liquibase" % "liquibase-core" % "3.4.0",
    "org.specs2" %% "specs2-core" % "2.3.11" % "test",
    "org.scalaz" %% "scalaz-core" % "7.1.0",
    "org.reflections" % "reflections" % "0.9.10",
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  )
}

// ----------------------------------------------------------------------
// sbt-revolver
// ----------------------------------------------------------------------

Revolver.settings

// ----------------------------------------------------------------------
// sbt-bower
// ----------------------------------------------------------------------

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

target in bower := (resourceManaged in Compile).value / "org" / "moscatocms" / "static" / "bower_components"

resourceGenerators in Compile += bower.taskValue

// ----------------------------------------------------------------------
// Liquibase
// ----------------------------------------------------------------------

liquibaseUsername := "moscato"

liquibasePassword := "moscato"

liquibaseDriver := "org.postgresql.Driver"

liquibaseUrl := "jdbc:postgresql:moscato"

liquibaseChangelog := file("src/main/resources/org/moscatocms/migrations/moscato-changelog.xml")

// ----------------------------------------------------------------------
// Slick
// ----------------------------------------------------------------------

slick <<= slickCodeGenTask

sourceGenerators in Compile <+= slickCodeGenTask

lazy val slick = TaskKey[Seq[File]]("gen-tables")

lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = (dir / "main").getPath // place generated files in sbt's managed sources folder
  //val url = "jdbc:h2:mem:test;INIT=runscript from 'src/main/sql/create.sql'" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
  val url = "jdbc:postgresql:moscato"
  val jdbcDriver = "org.postgresql.Driver"
  val slickDriver = "slick.driver.PostgresDriver"
  val pkg = "org.moscatocms.model"
  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg), s.log))
  val fname = outputDir + "/" + pkg.replace(".", "/") + "/Tables.scala"
  Seq(file(fname))
}

// ----------------------------------------------------------------------
// Eclipse
// ----------------------------------------------------------------------

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

// ----------------------------------------------------------------------
// Project
// ----------------------------------------------------------------------

lazy val root = (project in file(".")).enablePlugins(SbtLiquibase, SbtBower)

