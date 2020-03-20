# kafka-player

Plays a file onto a Kafka topic - one line == one message.

### Build

Build the project by running `sbt assembly`. This produces an uber jar at `target/scala-2.12/kafka-player.jar`.

### Invocation

For running against a local Kafka, invoke the player with a command like below:

```
java -jar $jar_file \
  --message-file $message_file \
  --num-messages $num_messages \
  --messages-per-second $messages_per_second \
  --topic $topic \
  --broker-string $broker_string
```

If running against a TLS authenticated Kafka cluster, include additional parameters to specify the keystore location and keystore password:

```
  --keystore-location $keystore_location
  --keystore-passphrase $keystore_passphrase
```

#### Parameters

* `--message-file` - A file containing the lines to play onto Kafka. The player will play one message per line.
* `--num-messages` - The number of messages to play. The default is 100,000 if not provided.
* `--messages-per-second` - The number of messages to play per second. The default is 2 per second if not provided. Float values are supported (e.g. 0.5 to produce one message every two seconds).
* `--topic` - The topic to play the messages onto.
* `--broker-string` - The bootstrap broker string of the target cluster. The default is localhost:9092 if not provided.
* `--keystore-location` - The location of the keystore to use for TLS authentication. Only set this if TLS authentication is desired.
* `--keystore-passphrase` - The passphrase for the keystore. Only set this if TLS authentication is desired and a `keystore-location` is also provided.

### Local Kafka

A docker-compose file is included for local development for spinning up a single broker wurstmeister/kafka cluster (https://hub.docker.com/r/wurstmeister/kafka/) exposed to localhost.
Topics required for specific scenarios may be added to the `KAFKA_CREATE_TOPICS` environment variable in `docker-compose.yml`.
The required format is described in https://github.com/wurstmeister/kafka-docker#automatically-create-topics.

### Example

The `example` folder of this repository contains a data file called `example.json` and a launch script called `play-example.sh` to play the data file onto a local Kafka. Run the example with the following steps:

```
# start the kafka docker containers in the background - this will also create a topic called `example` on the cluster.
docker-compose up -d

# play the example data file onto the local kafka
./example/play-example.sh
```

Once the player starts, run `kafkacat` in another terminal to verify your messages are coming in.

```
kafkacat -C -b localhost:9092 -t example
```

The example plays 10,000 messages at a rate of one message every two seconds.

The `example.json` data file is an abridged and slightly transformed (into a line delimited format) version of American movies from https://github.com/jdorfman/awesome-json-datasets#movies.

