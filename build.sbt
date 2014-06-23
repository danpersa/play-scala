name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.couchbase.client" % "couchbase-client" % "1.4.2",
  "com.sksamuel.elastic4s" %% "elastic4s" % "1.2.1.1"
)