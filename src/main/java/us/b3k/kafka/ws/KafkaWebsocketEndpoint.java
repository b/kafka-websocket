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

package us.b3k.kafka.ws;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import us.b3k.kafka.ws.consumer.KafkaConsumer;
import us.b3k.kafka.ws.messages.BinaryMessage;
import us.b3k.kafka.ws.messages.BinaryMessage.*;
import us.b3k.kafka.ws.messages.TextMessage;
import us.b3k.kafka.ws.messages.TextMessage.*;
import us.b3k.kafka.ws.producer.KafkaProducer;

import javax.websocket.Session;
import javax.websocket.OnOpen;
import javax.websocket.OnMessage;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.CloseReason;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;

@ServerEndpoint(
    value = "/v1/topics/{topics}",
    subprotocols = {"kafka-text", "kafka-binary"},
    decoders = {BinaryMessageDecoder.class, TextMessageDecoder.class},
    encoders = {BinaryMessageEncoder.class, TextMessageEncoder.class},
    configurator = KafkaWebsocketEndpoint.Configurator.class
)
public class KafkaWebsocketEndpoint {
    private static Logger LOG = LoggerFactory.getLogger(KafkaWebsocketEndpoint.class);

    private Properties configProps;
    private KafkaConsumer consumer = null;

    public static Map<String, String> getQueryMap(String query)
    {
        Map<String, String> map = Maps.newHashMap();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] nameval = param.split("=");
                map.put(nameval[0], nameval[1]);
            }
        }
        return map;
    }

    private KafkaProducer producer() {
        return Configurator.getProducer();
    }

    @OnOpen
    @SuppressWarnings("unchecked")
    public void onOpen(final Session session) {
        String groupId;

        Properties sessionProps = (Properties) Configurator.getConsumerProps().clone();
        Map<String, String> queryParams = KafkaWebsocketEndpoint.getQueryMap(session.getQueryString());
        if (queryParams.containsKey("group.id")) {
            groupId = queryParams.get("group.id");
        } else {
            groupId = sessionProps.getProperty("group.id") + "-" +
                                    session.getId() + "-" +
                                    String.valueOf(System.currentTimeMillis());
        }
        sessionProps.setProperty("group.id", groupId);

        String topics = session.getPathParameters().get("topics");
        LOG.debug("Opening new session {}", session.getId());
        if (!topics.isEmpty()) {
            LOG.debug("Session {} topics are {}", session.getId(), topics);
            consumer = new KafkaConsumer(sessionProps, session);
            consumer.start();
        }
    }

    @OnClose
    public void onClose(final Session session) {
        if (consumer != null) {
            consumer.stop();
        }
    }

    @OnMessage
    public void onMessage(final BinaryMessage message, final Session session) {
        LOG.trace("Received binary message: topic - {}; message - {}", message.getTopic(), message.getMessage());
        producer().send(message.getTopic(), message.getMessage());
    }

    @OnMessage
    public void onMessage(final TextMessage message, final Session session) {
        LOG.trace("Received text message: topic - {}; message - {}", message.getTopic(), message.getMessage());
        producer().send(message.getTopic(), message.getMessage().getBytes(Charset.forName("UTF-8")));
    }

    private void closeSession(Session session, CloseReason reason) {
        try {
            session.close(reason);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class Configurator extends ServerEndpointConfig.Configurator
    {
        private static Properties consumerProps;
        private static Properties producerProps;
        private static KafkaProducer producer = null;

        public static void setKafkaProps(Properties consumerProps, Properties producerProps) {
            Configurator.consumerProps = consumerProps;
            Configurator.producerProps = producerProps;
        }

        public static Properties getConsumerProps() {
            return Configurator.consumerProps;
        }

        public static Properties getProducerProps() {
            return Configurator.producerProps;
        }

        public static KafkaProducer getProducer() {
            if (producer == null) {
                producer = new KafkaProducer(producerProps);
                producer.start();
            }
            return producer;
        }

        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException
        {
            T endpoint = super.getEndpointInstance(endpointClass);

            if (endpoint instanceof KafkaWebsocketEndpoint) {
                return endpoint;
            }
            throw new InstantiationException(
                    MessageFormat.format("Expected instanceof \"{0}\". Got instanceof \"{1}\".",
                            KafkaWebsocketEndpoint.class, endpoint.getClass()));
        }
    }
}

