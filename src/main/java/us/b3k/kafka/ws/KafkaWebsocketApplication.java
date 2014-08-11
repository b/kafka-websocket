package us.b3k.kafka.ws;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class KafkaWebsocketApplication extends Application<KafkaWebsocketConfiguration> {
    public static void main(String[] args) throws Exception {
        new KafkaWebsocketApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<KafkaWebsocketConfiguration> bootstrap) {

    }

    @Override
    public void run(KafkaWebsocketConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().disable();

        KafkaWebsocketServer server = new KafkaWebsocketServer(configuration);
        server.run();
    }
}
