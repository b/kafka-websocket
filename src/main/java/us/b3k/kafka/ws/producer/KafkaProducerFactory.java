package us.b3k.kafka.ws.producer;

import us.b3k.kafka.ws.transforms.Transform;

import java.util.Properties;

public class KafkaProducerFactory {
    private final KafkaProducer producer;

    static public KafkaProducerFactory create(Properties configProps, Class inputTransformClass) throws IllegalAccessException, InstantiationException {
        Transform inputTransform = (Transform)inputTransformClass.newInstance();
        inputTransform.initialize();
        KafkaProducer producer = new KafkaProducer(configProps, inputTransform);
        return new KafkaProducerFactory(producer);
    }

    private KafkaProducerFactory(KafkaProducer producer) {
        this.producer = producer;
    }

    public KafkaProducer getProducer() {
        return producer;
    }
}
