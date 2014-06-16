package us.b3k.kafka.ws.consumer;

import kafka.consumer.ConsumerConfig;
import us.b3k.kafka.ws.transforms.Transform;

import javax.websocket.Session;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaConsumerFactory {
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Properties configProps;
    private final Transform outputTransform;

    static public KafkaConsumerFactory create(Properties configProps, Class outputTransformClass) throws IllegalAccessException, InstantiationException {
        Transform outputTransform = (Transform)outputTransformClass.newInstance();
        outputTransform.initialize();
        return new KafkaConsumerFactory(configProps, outputTransform);
    }

    private KafkaConsumerFactory(Properties configProps, Transform outputTransform) {
        this.configProps = configProps;
        this.outputTransform = outputTransform;
    }

    public KafkaConsumer getConsumer(String groupId, final String topics, final Session session) {
        return getConsumer(groupId, Arrays.asList(topics.split(",")), session);
    }

    public KafkaConsumer getConsumer(String groupId, final List<String> topics, final Session session) {
        if (groupId.isEmpty()) {
            groupId = String.format("%s-%d", session.getId(), System.currentTimeMillis());
            if (configProps.containsKey("group.id")) {
                groupId = String.format("%s-%s", configProps.getProperty("group.id"), groupId);
            }
        }
        Properties sessionProps = (Properties)configProps.clone();
        sessionProps.setProperty("group.id", groupId);

        KafkaConsumer consumer = new KafkaConsumer(new ConsumerConfig(sessionProps), executorService, outputTransform, topics, session);
        consumer.start();
        return consumer;
    }
}
