import com.github.sbtliquibase.SbtLiquibase
import com.tuplejump.sbt.yeoman.Yeoman

organization := "org.moscatocms"

name := "moscato"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Atlassian Releases" at "https://maven.atlassian.com/public/",
  "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "1.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.0",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.3-P24-SNAPSHOT",
  "com.mohiva" %% "play-silhouette" % "3.0.0-RC1",
  "com.mohiva" %% "play-silhouette-testkit" % "3.0.0-RC1" % "test",
  specs2 % Test
)

lazy val root = (project in file(".")).
  enablePlugins(PlayScala, SbtLiquibase)

routesGenerator := InjectedRoutesGenerator

liquibaseUsername := "moscato"

liquibasePassword := "moscato"

liquibaseDriver := "org.postgresql.Driver"

liquibaseUrl := "jdbc:postgresql:moscato"

Yeoman.yeomanSettings
