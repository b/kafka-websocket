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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.b3k.kafka.ws.messages.AbstractMessage;
import us.b3k.kafka.ws.messages.BinaryMessage;
import us.b3k.kafka.ws.messages.TextMessage;
import us.b3k.kafka.ws.transforms.Transform;

import javax.websocket.Session;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KafkaWebsocketProducer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaWebsocketProducer.class);

    private Map<String, Object> producerConfig;
    private KafkaProducer producer;
    private Transform inputTransform;

    @SuppressWarnings("unchecked")
    public KafkaWebsocketProducer(Properties configProps) {
        this.producerConfig = new HashMap<String, Object>((Map)configProps);
    }

    @SuppressWarnings("unchecked")
    public KafkaWebsocketProducer(Properties configProps, Transform inputTransform) {
        this.producerConfig = new HashMap<String, Object>((Map)configProps);
        this.inputTransform = inputTransform;
    }

    @SuppressWarnings("unchecked")
    public void start() {
        if (producer == null) {
            producer = new KafkaProducer(producerConfig, new StringSerializer(), new ByteArraySerializer());
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
        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, message);
        producer.send(record);
    }

    @SuppressWarnings("unchecked")
    public void send(String topic, String key, byte[] message) {
        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, message);
        producer.send(record);
    }
}
