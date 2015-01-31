resolvers += "web-plugin.repo" at "http://siasia.github.com/maven2"

resolvers += "bigtoast-github" at "http://bigtoast.github.com/repo/"

//libraryDependencies <+= sbtVersion(v => "com.earldouglas" % "xsbt-web-plugin" % "0.3.0" exclude("commons-logging", "commons-logging"))

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "1.0.0")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "2.3.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.github.bigtoast" % "sbt-liquibase" % "0.5")

autoCompilerPlugins := true

