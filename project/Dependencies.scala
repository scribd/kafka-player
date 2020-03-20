import sbt._

object Dependencies {
  lazy val scalaTest =  "org.scalatest" %% "scalatest" % "3.0.8" % Test

  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  lazy val scallop = "org.rogach" %% "scallop" % "3.3.1"
  lazy val kafkaClients = "org.apache.kafka" % "kafka-clients" % "2.2.1"
  lazy val guava = "com.google.guava" % "guava" % "28.0-jre"
}
