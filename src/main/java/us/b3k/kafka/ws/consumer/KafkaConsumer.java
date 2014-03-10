/*
    Copyright 2014 Benjamin Black

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package us.b3k.kafka.ws.consumer;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.b3k.kafka.ws.messages.BinaryMessage;
import us.b3k.kafka.ws.messages.TextMessage;

import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaConsumer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaConsumer.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private Session session;
    private ConsumerConfig consumerConfig;
    private ConsumerConnector connector;
    private List<String> topics;
    private Async remoteEndpoint;

    public KafkaConsumer(Properties configProps, Session session) {
        this.remoteEndpoint = session.getAsyncRemote();
        this.consumerConfig = new ConsumerConfig(configProps);
        String topicString = session.getPathParameters().get("topics");
        this.topics = Arrays.asList(topicString.split(","));
        this.session = session;
    }

    public void start() {
        LOG.debug("Starting consumer for {}", session.getId());
        this.connector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

        Map<String, Integer> topicCountMap = new HashMap<>();
        for (String topic : topics) {
            topicCountMap.put(topic, 1);
        }
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = connector.createMessageStreams(topicCountMap);

        for (String topic : topics) {
            LOG.debug("Adding stream for session {}, topic {}",session.getId(), topic);
            final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
            for (KafkaStream<byte[], byte[]> stream : streams) {
                executorService.submit(new KafkaConsumerTask(stream, remoteEndpoint, session));
            }
        }
    }

    public void stop() {
        LOG.debug("Stopping consumer for session {}", session.getId());
        connector.commitOffsets();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            LOG.error("Exception while waiting to shutdown consumer: {}", ie.getMessage());
        }
        if (connector != null) {
            LOG.trace("Shutting down connector for session {}", session.getId());
            connector.shutdown();
        }
        if (executorService != null) {
            LOG.trace("Shutting down executor for session {}", session.getId());
            executorService.shutdown();
        }
        LOG.debug("Stopped consumer for session {}", session.getId());
    }

    static public class KafkaConsumerTask implements Runnable {
        private KafkaStream stream;
        private Async remoteEndpoint;
        private Session session;

        public KafkaConsumerTask(KafkaStream stream, Async remoteEndpoint, Session session) {
            this.stream = stream;
            this.remoteEndpoint = remoteEndpoint;
            this.session = session;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            String subprotocol = session.getNegotiatedSubprotocol();
            for (MessageAndMetadata<byte[], byte[]> messageAndMetadata : (Iterable<MessageAndMetadata<byte[], byte[]>>) stream) {
                String topic = messageAndMetadata.topic();
                byte[] message = messageAndMetadata.message();
                switch(subprotocol) {
                    case "kafka-binary":
                        sendBinary(topic, message);
                        break;
                    case "kafka-text":
                        sendText(topic, message);
                        break;
                }
            }
        }

        private void sendBinary(String topic, byte[] message) {
            remoteEndpoint.sendObject(new BinaryMessage(topic, message));
        }

        private void sendText(String topic, byte[] message) {
            String messageString = new String(message, Charset.forName("UTF-8"));
            remoteEndpoint.sendObject(new TextMessage(topic, messageString));
        }

        private void closeSession(Exception e) {
            LOG.debug("Consumer initiated close of session {}", session.getId());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, e.getMessage()));
            } catch (IOException ioe) {
                LOG.error("Error closing session: {}", ioe.getMessage());
            }
        }
    }
}
