name := "StreamingExample"

version := "1.0"

scalaVersion := "2.10.4"

resolvers += "Confluent Maven Repo" at "http://packages.confluent.io/maven/"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.6.0" % "provided",
  "org.apache.spark" %% "spark-sql" % "1.6.0" % "provided",
  "org.apache.spark" %% "spark-streaming" % "1.6.0" % "provided",
  "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.6.0" exclude("org.spark-project.spark", "unused"),
  "io.confluent" % "kafka-avro-serializer" % "2.0.1"
)
