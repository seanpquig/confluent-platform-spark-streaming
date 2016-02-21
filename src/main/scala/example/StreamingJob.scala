package example

import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.KafkaAvroDecoder
import kafka.serializer.StringDecoder
import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SQLContext
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
    .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

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

  // Load JSON strings into DataFrame
  kafkaStream.foreachRDD { rdd =>
    // Get the singleton instance of SQLContext
    val sqlContext = SQLContext.getOrCreate(rdd.sparkContext)
    import sqlContext.implicits._

    val topicValueStrings = rdd.map(_._2.toString)
    val df = sqlContext.read.json(topicValueStrings)

    df.printSchema()
    println("DataFrame count: " + df.count())
    df.take(1).foreach(println)
  }

  ssc.start()
  ssc.awaitTermination()

}
