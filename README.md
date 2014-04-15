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

By default, a new, unique group.id is generated per session. The group.id for a consumer can be controlled by passing a
group.id as a query param: ?group.id=my_group_id

## Producing to topics

Clients publish to topics by connecting to /v1/topics/ and sending either text or binary messages that include a topic
and a message. Text messages may optionally include a key to influence the mapping of messages to partitions. A client
need not subscribe to a topic to publish to it.

## Binary messages

Binary messages are formatted as:

[topic name length byte][topic name bytes (UTF-8)][message bytes]

## Text messages

Text messages are JSON objects with two mandatory attributes: topic and message. They may also include an optional key
attribute:

{ "topic" : "my_topic", "message" : "my amazing message" }

{ "topic" : "my_topic", "key" : "my_key123", "message" : "my amazing message" }

## Configuration

See property files in conf/

## License

kafka-websocket is copyright 2014 Benjamin Black, and distributed under the Apache License 2.0.