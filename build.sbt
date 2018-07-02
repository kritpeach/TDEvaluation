name := """evalution"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.0-M1" % Test
// https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.1"

// https://mvnrepository.com/artifact/com.typesafe.play/play-slick-evolutions
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "4.0.0-M1"

// https://mvnrepository.com/artifact/com.typesafe.play/play-slick
libraryDependencies += "com.typesafe.play" %% "play-slick" % "4.0.0-M1"

// https://mvnrepository.com/artifact/com.github.tminglei/slick-pg
libraryDependencies += "com.github.tminglei" %% "slick-pg" % "0.16.2"



