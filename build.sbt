name := """api"""
organization := "ru"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.8"

crossScalaVersions := Seq("2.12.8", "2.11.12")

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test
libraryDependencies += "com.pauldijou" %% "jwt-play" % "2.1.0"
libraryDependencies += "com.auth0" % "jwks-rsa" % "0.6.1"