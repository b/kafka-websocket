package us.b3k.kafka.ws.consumer;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.apache.log4j.Logger;
import us.b3k.kafka.ws.messages.BinaryMessage;
import us.b3k.kafka.ws.messages.TextMessage;

import javax.websocket.CloseReason;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaConsumer {
    private static Logger LOG = Logger.getLogger(KafkaConsumer.class);

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private Session session;
    private ConsumerConfig consumerConfig;
    private ConsumerConnector connector;
    private List<String> topics;
    private Async remoteEndpoint;

    public KafkaConsumer(Properties configProps, Session session) {
        this.consumerConfig = new ConsumerConfig(configProps);
        this.remoteEndpoint = session.getAsyncRemote();
        String topicString = session.getPathParameters().get("topics");
        this.topics = Arrays.asList(topicString.split(","));
        this.session = session;
    }

    public void start() {
        LOG.debug("Starting new consumer");
        this.connector = kafka.consumer.Consumer.createJavaConsumerConnector(consumerConfig);

        Map<String, Integer> topicCountMap = new HashMap<>();
        for (String topic : topics) {
            topicCountMap.put(topic, 1);
        }
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = connector. createMessageStreams(topicCountMap);

        for (String topic : topics) {
            LOG.debug("    adding consumer for topic " + topic);
            final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
            for (KafkaStream<byte[], byte[]> stream : streams) {
                executorService.submit(new KafkaConsumerTask(stream, remoteEndpoint, session));
            }
        }
    }

    public void stop() {
        executorService.shutdown();
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
                    case "binary":
                        sendBinary(topic, message);
                        break;
                    case "text":
                        sendText(topic, message);
                        break;
                }
            }
        }

        private void sendBinary(String topic, byte[] message) {
            remoteEndpoint.sendObject(new BinaryMessage(topic, message));
        }

        private void sendText(String topic, byte[] message) {
            try {
                String messageString = new String(message,"UTF-8");
                remoteEndpoint.sendObject(new TextMessage(topic, messageString));
            } catch (UnsupportedEncodingException e) {
                closeSession(e);
            }
        }

        private void closeSession(Exception e) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, e.getMessage()));
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
