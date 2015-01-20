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

package us.b3k.kafka.ws.producer;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.b3k.kafka.ws.messages.AbstractMessage;
import us.b3k.kafka.ws.messages.BinaryMessage;
import us.b3k.kafka.ws.messages.TextMessage;
import us.b3k.kafka.ws.transforms.Transform;

import javax.websocket.Session;
import java.nio.charset.Charset;
import java.util.Properties;

public class KafkaProducer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaProducer.class);

    private ProducerConfig producerConfig;
    private Producer producer;
    private Transform inputTransform;

    public KafkaProducer(Properties configProps) {
        this.producerConfig = new ProducerConfig(configProps);
    }

    public KafkaProducer(Properties configProps, Transform inputTransform) {
        this.producerConfig = new ProducerConfig(configProps);
        this.inputTransform = inputTransform;
    }

    public void start() {
        if (producer == null) {
            producer = new Producer(producerConfig);
        }
    }

    public void stop() {
        producer.close();
        producer = null;
    }

    private void send(final AbstractMessage message) {
        if(!message.isDiscard()) {
            if (message.isKeyed()) {
                send(message.getTopic(), message.getKey(), message.getMessageBytes());
            } else {
                send(message.getTopic(), message.getMessageBytes());
            }
        }
    }

    public void send(final BinaryMessage message, final Session session) {
        send(inputTransform.transform(message, session));
    }

    public void send(final TextMessage message, final Session session) {
        send(inputTransform.transform(message, session));
    }

    @SuppressWarnings("unchecked")
    public void send(String topic, byte[] message) {
        final KeyedMessage<String, byte[]> keyedMessage = new KeyedMessage<>(topic, message);
        producer.send(keyedMessage);
    }

    @SuppressWarnings("unchecked")
    public void send(String topic, String key, byte[] message) {
        final KeyedMessage<String, byte[]> keyedMessage = new KeyedMessage<>(topic, key, message);
        producer.send(keyedMessage);
    }
}
