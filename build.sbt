name := """evalution"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5"
// https://mvnrepository.com/artifact/com.typesafe.play/play-slick
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.3"

libraryDependencies += "com.typesafe.slick" %% "slick" % "3.2.3"

libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.15.5"
// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.1.4"




