resolvers += "web-plugin.repo" at "http://siasia.github.com/maven2"

resolvers += "bigtoast-github" at "http://bigtoast.github.com/repo/"

libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % ("0.12.0-0.2.11.1") exclude("commons-logging", "commons-logging"))

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.1.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.6.0")

addSbtPlugin("atd" % "sbt-liquibase" % "0.4")

autoCompilerPlugins := true


