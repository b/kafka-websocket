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

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.b3k.kafka.ws.consumer.KafkaConsumerFactory;
import us.b3k.kafka.ws.producer.KafkaProducerFactory;

import javax.websocket.server.ServerContainer;

public class KafkaWebsocketServer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaWebsocketServer.class);

    private final KafkaWebsocketConfiguration configuration;

    public KafkaWebsocketServer(KafkaWebsocketConfiguration configuration) {
        this.configuration = configuration;
    }

    private SslContextFactory newSslContextFactory() {
        LOG.info("Configuring TLS");
        KafkaWebsocketConfiguration.SSLConfiguration sslConfig = configuration.getSsl();

        String keyStorePath = sslConfig.getKeyStorePath();
        String keyStorePassword = sslConfig.getKeyStorePassword();
        String trustStorePath = sslConfig.getTrustStorePath();
        String trustStorePassword = sslConfig.getTrustStorePassword();
        String[] protocols = sslConfig.getProtocols();
        String[] ciphers = sslConfig.getProtocols();
        String clientAuth = sslConfig.getClientAuth();

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keyStorePath);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyStorePassword);
        sslContextFactory.setTrustStorePath(trustStorePath);
        sslContextFactory.setTrustStorePassword(trustStorePassword);
        sslContextFactory.setIncludeProtocols(protocols);
        sslContextFactory.setIncludeCipherSuites(ciphers);
        switch(clientAuth) {
            case "required":
                LOG.info("Client auth required.");
                sslContextFactory.setNeedClientAuth(true);
                sslContextFactory.setValidatePeerCerts(true);
                break;
            case "optional":
                LOG.info("Client auth allowed.");
                sslContextFactory.setWantClientAuth(true);
                sslContextFactory.setValidatePeerCerts(true);
                break;
            default:
                LOG.info("Client auth disabled.");
                sslContextFactory.setNeedClientAuth(false);
                sslContextFactory.setWantClientAuth(false);
                sslContextFactory.setValidatePeerCerts(false);
        }
        return sslContextFactory;
    }

    private ServerConnector newSslServerConnector(Server server) {
        Integer securePort = configuration.getSsl().getPort();
        HttpConfiguration https = new HttpConfiguration();
        https.setSecureScheme("https");
        https.setSecurePort(securePort);
        https.setOutputBufferSize(32768);
        https.setRequestHeaderSize(8192);
        https.setResponseHeaderSize(8192);
        https.setSendServerVersion(true);
        https.setSendDateHeader(false);
        https.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory sslContextFactory = newSslContextFactory();
        ServerConnector sslConnector =
                new ServerConnector(server,
                        new SslConnectionFactory(sslContextFactory, "HTTP/1.1"), new HttpConnectionFactory(https));
        sslConnector.setPort(securePort);
        return sslConnector;
    }

    public void run() {
        try {
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(configuration.getPort());
            server.addConnector(connector);

            if(configuration.getSsl().isEnabled()) {
                server.addConnector(newSslServerConnector(server));
            }

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            ServerContainer wsContainer = WebSocketServerContainerInitializer.configureContext(context);
            String inputTransformClassName = configuration.getInputTransformClass();
            String outputTransformClassName = configuration.getOutputTransformClass();
            KafkaConsumerFactory consumerFactory =
                    KafkaConsumerFactory.create(configuration.getConsumer(), Class.forName(outputTransformClassName));
            KafkaProducerFactory producerFactory =
                    KafkaProducerFactory.create(configuration.getProducer(), Class.forName(inputTransformClassName));

            KafkaWebsocketEndpoint.Configurator.CONSUMER_FACTORY = consumerFactory;
            KafkaWebsocketEndpoint.Configurator.PRODUCER = producerFactory.getProducer();

            wsContainer.addEndpoint(KafkaWebsocketEndpoint.class);

            server.start();
            server.join();
        } catch (Exception e) {
            LOG.error("Failed to start the server: {}", e.getMessage());
        }
    }
}
