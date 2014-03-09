package us.b3k.kafka.ws.producer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaProducer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaProducer.class);

    private ProducerConfig producerConfig;
    private Producer producer;

    public KafkaProducer(Properties configProps) {
        this.producerConfig = new ProducerConfig(configProps);
    }

    public void start() {
        this.producer = new Producer(producerConfig);
    }

    public void stop() {
        producer.close();
    }

    @SuppressWarnings("unchecked")
    public void send(String topic, byte[] message) {
        final KeyedMessage<byte[], byte[]> keyedMessage = new KeyedMessage<>(topic, message);
        producer.send(keyedMessage);
    }
}
