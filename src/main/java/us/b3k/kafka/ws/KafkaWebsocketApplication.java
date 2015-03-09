package us.b3k.kafka.ws;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.b3k.kafka.ws.consumer.KafkaConsumerFactory;
import us.b3k.kafka.ws.producer.KafkaProducerFactory;

import javax.websocket.server.ServerContainer;

public class KafkaWebsocketApplication extends Application<KafkaWebsocketConfiguration> {
    private static Logger LOG = LoggerFactory.getLogger(KafkaWebsocketServer.class);

    public static void main(String[] args) throws Exception {
        new KafkaWebsocketApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<KafkaWebsocketConfiguration> bootstrap) {

    }

    @Override
    public void run(KafkaWebsocketConfiguration configuration, Environment environment) throws Exception {
        String inputTransformClassName = configuration.getInputTransformClass();
        String outputTransformClassName = configuration.getOutputTransformClass();
        KafkaConsumerFactory consumerFactory =
                KafkaConsumerFactory.create(configuration.getConsumer(), Class.forName(outputTransformClassName));
        KafkaProducerFactory producerFactory =
                KafkaProducerFactory.create(configuration.getProducer(), Class.forName(inputTransformClassName));

        KafkaWebsocketEndpoint.Configurator.CONSUMER_FACTORY = consumerFactory;
        KafkaWebsocketEndpoint.Configurator.PRODUCER = producerFactory.getProducer();

        ServerContainer wsContainer =
                WebSocketServerContainerInitializer.configureContext();
        wsContainer.addEndpoint(KafkaWebsocketEndpoint.class);

        KafkaWebsocketServer server =
                new KafkaWebsocketServer(configuration, environment.getApplicationContext().getServer());
        environment.lifecycle().manage(server);

        environment.jersey().disable();
        //environment.jersey().register(new BrokerResource());
    }
}
