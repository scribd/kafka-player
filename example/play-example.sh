#!/bin/bash

#
# Runs the `kafka-player` application against a local Kafka.
#

set -eu

scriptpath="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

jar_file=$scriptpath/../target/scala-2.12/kafka-player.jar
message_file=$scriptpath/example.json
num_messages=10000
messages_per_second=0.5
topic=example
broker_string=localhost:9092

if [[ ! -f $scriptpath/example.json ]]; then
	echo "Unzipping example.json"
	tar -xzvf $scriptpath/example.json.tar.gz -C $scriptpath/
fi

echo "Starting kafka-player"

java -jar $jar_file \
  --message-file $message_file \
  --num-messages $num_messages \
  --messages-per-second $messages_per_second \
  --topic $topic \
  --broker-string $broker_string
