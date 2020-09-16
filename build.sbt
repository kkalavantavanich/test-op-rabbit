name := "test-op-rabbit"

version := "1.0"

scalaVersion := "2.12.11"

lazy val typesafeConfigVersion = "1.4.0"
lazy val scalaTestVersion = "3.1.1"
lazy val akkaStreamVersion = "2.6.4"
lazy val akkaVersion = "2.6.3"
lazy val mockitoVersion = "1.13.10"
lazy val opRabbitVersion = "2.1.0"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % typesafeConfigVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaStreamVersion,
  "com.typesafe.akka" %% "akka-actor" % "2.6.4",
  "com.spingo" %% "op-rabbit-core" % opRabbitVersion,
  "com.spingo" %% "op-rabbit-akka-stream" % opRabbitVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "org.mockito" %% "mockito-scala-scalatest" % mockitoVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaStreamVersion % Test
)
