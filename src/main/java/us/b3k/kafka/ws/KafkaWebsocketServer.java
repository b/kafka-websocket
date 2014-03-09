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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import java.io.IOException;
import java.util.Properties;

public class KafkaWebsocketServer {
    private static Logger LOG = LoggerFactory.getLogger(KafkaWebsocketServer.class);

    private static final String DEFAULT_PORT = "8080";

    private final Properties wsProps;
    private final Properties consumerProps;
    private final Properties producerProps;

    public KafkaWebsocketServer(Properties wsProps, Properties consumerProps, Properties producerProps) {
        this.wsProps = wsProps;
        this.consumerProps = consumerProps;
        this.producerProps = producerProps;
    }

    public void run() {
        try {
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(Integer.parseInt(wsProps.getProperty("ws.port", DEFAULT_PORT)));
            server.addConnector(connector);
            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            ServerContainer wsContainer = WebSocketServerContainerInitializer.configureContext(context);
            KafkaWebsocketEndpoint.Configurator.setKafkaProps(consumerProps, producerProps);
            wsContainer.addEndpoint(KafkaWebsocketEndpoint.class);

            server.start();
            //server.dump(System.err);
            server.join();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DeploymentException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
