val liftVersion = "2.6"

organization := "org.moscatocms"

name := "moscato"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

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
  "org.squeryl" %% "squeryl" % "0.9.5-6",
  "org.apache.derby" % "derby" % "10.9.1.0",
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "net.liftweb" %% "lift-squeryl-record" % liftVersion,
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

liquibaseChangelog := "repo/src/main/migrations/changelog.xml"

jetty()
