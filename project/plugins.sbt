resolvers ++= Seq(
  //"web-plugin.repo" at "http://siasia.github.com/maven2",
  "bigtoast" at "http://bigtoast.github.com/repo/",
  Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
  "sonatype-snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "1.1.1")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.5.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

//addSbtPlugin("com.github.bigtoast" % "sbt-liquibase" % "0.5")

addSbtPlugin("com.github.sbtliquibase" % "sbt-liquibase" % "0.1.0-SNAPSHOT")

autoCompilerPlugins := true

