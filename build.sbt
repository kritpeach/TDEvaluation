name := """evalution"""
organization := "com.example"
version := "1.0-SNAPSHOT"
lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"
libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.3"
libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.16.3"
libraryDependencies += "org.postgresql" % "postgresql" % "42.1.4"


