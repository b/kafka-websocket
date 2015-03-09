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
import us.b3k.kafka.ws.producer.KafkaWebsocketProducerFactory;

import javax.websocket.server.ServerContainer;
import java.util.Properties;

public class KafkaWebsocketServer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaWebsocketServer.class);

    private static final String DEFAULT_PORT = "8080";
    private static final String DEFAULT_SSL_PORT = "8443";
    private static final String DEFAULT_PROTOCOLS = "TLSv1.2";
    private static final String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_RC4_128_SHA,TLS_RSA_WITH_AES_256_CBC_SHA";

    private final Properties wsProps;
    private final Properties consumerProps;
    private final Properties producerProps;

    public KafkaWebsocketServer(Properties wsProps, Properties consumerProps, Properties producerProps) {
        this.wsProps = wsProps;
        this.consumerProps = consumerProps;
        this.producerProps = producerProps;
    }

    private SslContextFactory newSslContextFactory() {
        LOG.info("Configuring TLS.");
        String keyStorePath = wsProps.getProperty("ws.ssl.keyStorePath");
        String keyStorePassword = wsProps.getProperty("ws.ssl.keyStorePassword");
        String trustStorePath = wsProps.getProperty("ws.ssl.trustStorePath", keyStorePath);
        String trustStorePassword = wsProps.getProperty("ws.ssl.trustStorePassword", keyStorePassword);
        String[] protocols = wsProps.getProperty("ws.ssl.protocols", DEFAULT_PROTOCOLS).split(",");
        String[] ciphers = wsProps.getProperty("ws.ssl.ciphers", DEFAULT_CIPHERS).split(",");
        String clientAuth = wsProps.getProperty("ws.ssl.clientAuth", "none");

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
        Integer securePort = Integer.parseInt(wsProps.getProperty("ws.ssl.port", DEFAULT_SSL_PORT));
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
            connector.setPort(Integer.parseInt(wsProps.getProperty("ws.port", DEFAULT_PORT)));
            server.addConnector(connector);

            if(Boolean.parseBoolean(wsProps.getProperty("ws.ssl", "false"))) {
                server.addConnector(newSslServerConnector(server));
            }

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            ServerContainer wsContainer = WebSocketServerContainerInitializer.configureContext(context);
            String inputTransformClassName =
                    wsProps.getProperty("ws.inputTransformClass", "us.b3k.kafka.ws.transforms.Transform");
            String outputTransformClassName =
                    wsProps.getProperty("ws.outputTransformClass", "us.b3k.kafka.ws.transforms.Transform");
            KafkaConsumerFactory consumerFactory =
                    KafkaConsumerFactory.create(consumerProps, Class.forName(outputTransformClassName));
            KafkaWebsocketProducerFactory producerFactory =
                    KafkaWebsocketProducerFactory.create(producerProps, Class.forName(inputTransformClassName));

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
