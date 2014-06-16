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

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.util.Properties;

public class KafkaWebsocketMain {
    private static Logger LOG = LoggerFactory.getLogger(KafkaWebsocketMain.class);

    private static final String LOG4J_PROPS_PATH = "conf/log4j.properties";
    private static final String SERVER_PROPS_PATH = "conf/server.properties";
    private static final String CONSUMER_PROPS_PATH = "conf/consumer.properties";
    private static final String PRODUCER_PROPS_PATH = "conf/producer.properties";

    private static Properties loadPropsFromFile(String filename) {
        try {
            Properties props = new Properties();
            props.load(new FileInputStream(filename));
            return props;
        } catch (java.io.IOException e) {
            LOG.error("Failed to load properties from file {}, exiting: {}", filename, e.getMessage());
            System.exit(-1);
        }
        return null;
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure(LOG4J_PROPS_PATH);
        Properties wsProps = loadPropsFromFile(SERVER_PROPS_PATH);
        Properties consumerProps = loadPropsFromFile(CONSUMER_PROPS_PATH);
        Properties producerProps = loadPropsFromFile(PRODUCER_PROPS_PATH);

        KafkaWebsocketServer server = new KafkaWebsocketServer(wsProps, consumerProps, producerProps);
        server.run();
    }
}
