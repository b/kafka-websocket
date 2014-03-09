# kafka-websocket

kafka-websocket is a simple websocket server interface to the kafka distributed message broker. It supports clients
subscribing to topics, including multiple topics at once, and sending messages to topics. Messages may be either text
or binary, the format for each is described below.

A client may produce and consume messages on the same connection.

## Consuming from topics

Clients subscribe to topics by specifying them in the path used when connecting to kafka-websocket. The path is:

/v1/topics/{topics}

where topics is a comma-separated list of topic names. If no topics are given, the client will not receive messages.
The format of messages sent to clients is determined by the subprotocol negotiated: kafka-text or kafka-binary. If no
subprotocol is specified, kafka-text is used.

## Producing to topics

Clients publish to topics by connecting to /v1/topics/ and sending either text or binary messages that include a topic
and a message. A client need not subscribe to a topic to publish to it.

## Binary messages

Binary messages are formatted as:

[null terminated topic name string][message]

This will likely change in the near future.

## Text messages

Text messages are JSON objects with two attributes: topic and message.

{ "topic": "my_topic", "message": "my amazing message" }

## Configuration

See property files in conf/
