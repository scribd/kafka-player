package com.scribd.streams.kafka.player

import com.google.common.util.concurrent.RateLimiter
import com.typesafe.scalalogging.Logger
import org.apache.kafka.clients.producer.{Producer, ProducerRecord}

import scala.io.BufferedSource

/**
 * Plays files onto Kafka topics.
 */
object FilePlayer {
  lazy val logger: Logger = Logger(getClass)

  /**
   * Starts playing the given message source onto the topic.
   *
   * @param producer the producer client to send messages through
   * @param messageSource the message source to read messages from
   * @param numMessages the total number of messages to play onto the topic
   * @param rateLimiter the rate limiter to control message send rate
   * @param topic the topic to play messages onto
   */
  def play(producer: Producer[String, String], messageSource: BufferedSource, numMessages: Int, rateLimiter: RateLimiter, topic: String): Unit = {
    var generatedMessageCount = 0

    logger.info(s"Will play $numMessages messages. Rate limited to ${rateLimiter.getRate} messages per second on $topic.")

    messageSource.getLines().foreach(m => {
      if (!m.isEmpty) {
        // only produce the desired number of messages per second.
        rateLimiter.acquire(1)

        val producerRecord = new ProducerRecord[String, String](topic, m)

        val recordMetadata = producer.send(producerRecord).get()

        generatedMessageCount += 1

        if (generatedMessageCount % 1000 == 0) {
          logger.info(s"Played $generatedMessageCount events so far.")
          logger.info(s"Last timestamp played is ${recordMetadata.timestamp()} at offset ${recordMetadata.offset()} on partition ${recordMetadata.partition()}")
        }
      }

      // stop if we've played the desired number of events.
      if (generatedMessageCount >= numMessages) {
        logger.info(s"Played $generatedMessageCount messages. Stopping.")

        return
      }
    })
  }
}
