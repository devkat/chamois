import com.github.sbtliquibase.SbtLiquibase

val liftVersion = "2.6"

organization := "org.moscatocms"

name := "moscato"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.5"

scalacOptions += "-deprecation"

resolvers ++= Seq(
  //ScalaToolsReleases,
  "BeCompany Nexus" at "http://nexus.becompany.ch/nexus/content/groups/public/",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Shiro Releases" at "https://repository.apache.org/content/repositories/releases/",
  "Shiro Snapshots" at "https://repository.apache.org/content/repositories/snapshots/",
  "sonatype.repo" at "https://oss.sonatype.org/content/repositories/public/"
)

libraryDependencies ++= Seq(
  "org.apache.tika" % "tika-parsers" % "1.4"
    excludeAll(ExclusionRule(organization = "org.bouncycastle"), ExclusionRule(organization = "org.aspectj")),
  "com.typesafe.slick" %% "slick" % "3.0.0",
  "com.typesafe.slick" %% "slick-codegen" % "3.0.0",
  "org.apache.derby" % "derby" % "10.9.1.0",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "net.liftweb" %% "lift-webkit" % liftVersion,
  "net.devkat" %% "lift-bootstrap" % "0.1.0",
  "org.clapper" %% "classutil" % "1.0.2",
  "eu.getintheloop" %% ("lift-shiro_" + liftVersion) % "0.0.8-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "net.liftmodules" %% "openid" % "2.5-RC4-1.2" excludeAll(ExclusionRule(organization = "net.liftweb")),
  "se.fishtank" %% "css-selectors-scala" % "0.1.2",
  "commons-collections" % "commons-collections" % "3.2.1"
)

liquibaseUrl := "jdbc:postgresql:moscato"

liquibaseDriver := "org.postgresql.Driver"

liquibaseUsername := "moscato"

liquibasePassword := "moscato"

jetty()

enablePlugins(SbtLiquibase)

slick <<= slickCodeGenTask

sourceGenerators in Compile <+= slickCodeGenTask

// code generation task
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

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Managed

EclipseKeys.withBundledScalaContainers := false
