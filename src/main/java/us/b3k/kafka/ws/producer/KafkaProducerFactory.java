package us.b3k.kafka.ws.producer;

import us.b3k.kafka.ws.transforms.Transform;

import java.util.Properties;

public class KafkaProducerFactory {
    private final Properties configProps;
    private final Transform inputTransform;
    private KafkaProducer producer;

    static public KafkaProducerFactory create(Properties configProps, Class inputTransformClass) throws IllegalAccessException, InstantiationException {
        Transform inputTransform = (Transform)inputTransformClass.newInstance();
        inputTransform.initialize();

        return new KafkaProducerFactory(configProps, inputTransform);
    }

    private KafkaProducerFactory(Properties configProps, Transform inputTransform) {
        this.configProps = configProps;
        this.inputTransform = inputTransform;
    }

    public KafkaProducer getProducer() {
        if (producer == null) {
            producer = new KafkaProducer(configProps, inputTransform);
            producer.start();
        }
        return producer;
    }
}
