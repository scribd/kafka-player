import Dependencies._

name := "kafka-player"
organization := "com.scribd"
version := "0.1.0"
scalaVersion := "2.12.10"
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"

libraryDependencies ++= Seq(
  scalaTest,
  logback,
  scalaLogging,
  scallop,
  kafkaClients,
  guava
)