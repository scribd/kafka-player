package com.scribd.streams.kafka.player

import java.util.Properties

import com.google.common.util.concurrent.RateLimiter
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.{KafkaProducer, Producer, ProducerConfig}
import org.apache.kafka.common.serialization.StringSerializer
import org.rogach.scallop.{ScallopConf, ScallopOption}

import scala.io.Source

/**
 * Main driver for the application.
 */
object Driver {

  /**
   * Command line options for the application.
   *
   * @param arguments
   */
  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val messageFile: ScallopOption[String] = opt[String](required = true,
      descr = "A file containing the lines to play onto Kafka. The player will play one message per line.")
    val numMessages: ScallopOption[Int] = opt[Int](default = Some(100000),
      descr = "The number of messages to play. The default is 100,000 if not provided.")
    val messagesPerSecond: ScallopOption[Float] = opt[Float](default = Some(2.0f),
      descr = "The number of messages to play per second. The default is 2 per second if not provided. Float values are supported (e.g. 0.5 to produce one message every two seconds).")
    val topic: ScallopOption[String] = opt[String](required = true,
      descr = "The topic to play the messages onto.")
    val brokerString: ScallopOption[String] = opt[String](default = Some("localhost:9092"),
      descr = "The bootstrap broker string representing for the target cluster. The default is localhost:9092 if not provided.")
    val keystoreLocation: ScallopOption[String] = opt[String](
      descr = "The location of the keystore to use for TLS authentication. Only set this if TLS authentication is desired.")
    val keystorePassphrase: ScallopOption[String] = opt[String](
      descr = "The passphrase for the keystore. Only set this if TLS authentication is desired and a `keystore-location` is also provided.")

    verify()
  }

  lazy val logger: Logger = Logger(getClass)

  def main(args: Array[String]): Unit = {
    val conf = new Conf(args)

    val rateLimiter: RateLimiter = RateLimiter.create(conf.messagesPerSecond())

    val kafkaProperties = createKafkaProperties(conf)
    val producer = createProducer(kafkaProperties)

    val messageSource = Source.fromFile(conf.messageFile())

    FilePlayer.play(producer, messageSource, conf.numMessages(), rateLimiter, conf.topic())

    producer.close()
    messageSource.close()

    sys.ShutdownHookThread {
      logger.warn("Shutting down producer from shutdown hook.")

      producer.close()
      messageSource.close()
    }
  }

  private def createKafkaProperties(conf: Conf): Properties = {
    val kafkaProperties = new Properties()

    kafkaProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, conf.brokerString())
    kafkaProperties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip")

    if (conf.keystoreLocation.isDefined) {
      kafkaProperties.put("security.protocol", "SSL")
      kafkaProperties.put("ssl.keystore.location", conf.keystoreLocation())
    }

    if (conf.keystorePassphrase.isDefined) {
      kafkaProperties.put("ssl.keystore.password", conf.keystorePassphrase())
    }

    kafkaProperties
  }

  private def createProducer(kafkaProperties: Properties): Producer[String, String] =
    new KafkaProducer[String, String](
      kafkaProperties,
      new StringSerializer(),
      new StringSerializer()
    )
}
