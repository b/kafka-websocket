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
import us.b3k.kafka.ws.messages.TextMessage;

import java.nio.charset.Charset;
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

    public void send(TextMessage message) {
        if (message.isKeyed()) {
            send(message.getTopic(), message.getKey(), message.getMessage().getBytes(Charset.forName("UTF-8")));
        } else {
            send(message.getTopic(), message.getMessage().getBytes(Charset.forName("UTF-8")));
        }
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
