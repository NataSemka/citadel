organization := "org.natasemka"
name := "citadel-server"
version := "0.1-SNAPSHOT"
scalaVersion := "2.12.4"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += guice
libraryDependencies += ws

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.6"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.6"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster" % "2.5.6"
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-tools" % "2.5.6"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.6" % Test
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "3.0.0" % Test
