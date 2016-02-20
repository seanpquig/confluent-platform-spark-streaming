package example

import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.KafkaAvroDecoder
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by squigley on 2/20/16.
  */
object StreamingJob extends App {

  // Get job configuration
  val config = ConfigFactory.load()

  Logger.getLogger("example").setLevel(Level.toLevel(config.getString("loglevel")))
  private val logger = Logger.getLogger(getClass)

  // Spark config and contexts
  val sparkMaster = config.getString("spark.master")
  val sparkConf = new SparkConf()
    .setMaster(sparkMaster)
    .setAppName("StreamingExample")

  val sc = new SparkContext(sparkConf)
  val batchInterval = config.getInt("spark.batch.interval")
  val ssc = new StreamingContext(sc, Seconds(batchInterval))

  // Create Kafka stream
  val groupId = config.getString("kafka.group.id")
  val topic = config.getString("topic")
  val kafkaParams = Map(
    "bootstrap.servers" -> config.getString("kafka.bootstrap.servers"),
    "schema.registry.url" -> config.getString("kafka.schema.registry.url"),
    "group.id" -> groupId
  )

  @transient val kafkaStream: DStream[(String, Object)] =
      KafkaUtils.createDirectStream[String, Object, StringDecoder, KafkaAvroDecoder](
        ssc, kafkaParams, Set(topic)
      )

  // Simply print first 5 elements and size of micro-batch
  kafkaStream.print(5)
  println("Elements in current micro-batch: " + kafkaStream.count())

  ssc.start()
  ssc.awaitTermination()

}
