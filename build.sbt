name := "slotbook-telegram-bot"
scalaVersion := "2.12.5"

val playWsStandaloneVersion: String = "2.0.0-M1"
resolvers += "lightshed-maven" at "http://dl.bintray.com/content/lightshed/maven"

libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsStandaloneVersion
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % playWsStandaloneVersion
libraryDependencies += "com.osinka.i18n" %% "scala-i18n" % "1.0.2"
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)
mainClass in Compile := Some("me.slotbook.QuickstartServer")
dockerBaseImage := "openjdk:jre-alpine"
dockerRepository := Some("localhost:5000")