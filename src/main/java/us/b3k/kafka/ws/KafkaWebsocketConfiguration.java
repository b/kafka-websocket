package us.b3k.kafka.ws;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import java.util.Properties;

public class KafkaWebsocketConfiguration extends Configuration {
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_SSL_PORT = 8443;
    private static final String[] DEFAULT_PROTOCOLS = {"TLSv1.2"};
    private static final String DEFAULT_CIPHERS = "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_RC4_128_SHA,TLS_RSA_WITH_AES_256_CBC_SHA";

    private static final Properties CONSUMER_DEFAULTS;
    static {
        Properties p = new Properties();
        p.setProperty("group.id", "kafka-websocket");
        p.setProperty("zookeeper.connect", "localhost:2181");
        p.setProperty("serializer.class", "kafka.serializer.DefaultEncoder");
        p.setProperty("key.serializer.class", "kafka.serializer.StringEncoder");
        CONSUMER_DEFAULTS = p;
    }

    private static final Properties PRODUCER_DEFAULTS;
    static {
        Properties p = new Properties();
        p.setProperty("metadata.broker.list", "localhost:9092");
        p.setProperty("request.required.acks", "1");
        p.setProperty("producer.type", "async");
        p.setProperty("serializer.class", "kafka.serializer.DefaultEncoder");
        p.setProperty("key.serializer.class", "kafka.serializer.StringEncoder");
        PRODUCER_DEFAULTS = p;
    }

    @JsonProperty
    private int port = DEFAULT_PORT;
    @JsonProperty
    private String inputTransformClass = "us.b3k.kafka.ws.transforms.Transform";
    @JsonProperty
    private String outputTransformClass = "us.b3k.kafka.ws.transforms.Transform";
    @JsonProperty
    private SSLConfiguration ssl = new SSLConfiguration();
    @JsonProperty
    private Properties consumer = new Properties(CONSUMER_DEFAULTS);
    @JsonProperty
    private Properties producer = new Properties(PRODUCER_DEFAULTS);

    public int getPort() {
        return port;
    }

    public String getInputTransformClass() {
        return inputTransformClass;
    }

    public String getOutputTransformClass() {
        return outputTransformClass;
    }

    public SSLConfiguration getSsl() {
        return ssl;
    }

    public Properties getConsumer() {
        return consumer;
    }

    public Properties getProducer() {
        return producer;
    }

    public class SSLConfiguration extends Configuration {
        @JsonProperty
        private Boolean enabled = false;
        @JsonProperty
        private int port = DEFAULT_SSL_PORT;
        @JsonProperty
        private String keyStorePath = "conf/keystore";
        @JsonProperty
        private String keyStorePassword = "password";
        @JsonProperty
        private String trustStorePath = "conf/keystore";
        @JsonProperty
        private String trustStorePassword = "password";
        @JsonProperty
        private String[] protocols = DEFAULT_PROTOCOLS;
        @JsonProperty
        private String ciphers = DEFAULT_CIPHERS;
        @JsonProperty
        private String clientAuth = "none";

        public Boolean isEnabled() {
            return enabled;
        }

        public int getPort() {
            return port;
        }

        public String getKeyStorePath() {
            return keyStorePath;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public String getTrustStorePath() {
            return trustStorePath;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public String[] getProtocols() {
            return protocols;
        }

        public String getCiphers() {
            return ciphers;
        }

        public String getClientAuth() {
            return clientAuth;
        }
    }
}
