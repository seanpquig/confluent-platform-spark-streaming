# confluent-platform-spark-streaming
Example of consuming Avro data encoded with Confluent serialziers from Kafka with Spark Streaming.

### Build it

	$ sbt assembly
	
### Running it
Export env variables for required configuration.

	$ export KAFKA_TOPIC=example_topic
    $ export BOOTSTRAP_SERVERS=example.server1.net:9092,example.server2.net:9092,example.server3.net:9092
    $ export SCHEMA_REGISTRY_URL=http://kafka-schema.registry.net:8081

Spark submit it

	$ spark-submit --class example.StreamingJob --driver-java-options "-Dconfig.file=conf/application.conf -Dlog4j.configuration=file:conf/log4j.properties" target/scala-2.10/StreamingExample-assembly-1.0.jar