name := "slotbook-telegram-bot"

version := "1.0"

scalaVersion := "2.12.5"

val playWsStandaloneVersion: String = "2.0.0-M1"

libraryDependencies += "info.mukel" %% "telegrambot4s" % "3.0.14"
libraryDependencies += "com.typesafe.play" %% "play-ahc-ws-standalone" % playWsStandaloneVersion
libraryDependencies += "com.typesafe.play" %% "play-ws-standalone-json" % playWsStandaloneVersion
libraryDependencies += "com.osinka.i18n" %% "scala-i18n" % "1.0.2"