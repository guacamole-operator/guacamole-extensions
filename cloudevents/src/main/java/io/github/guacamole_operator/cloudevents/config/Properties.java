package io.github.guacamole_operator.cloudevents.config;

import java.net.URI;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.environment.LocalEnvironment;
import org.apache.guacamole.properties.IntegerGuacamoleProperty;
import org.apache.guacamole.properties.StringGuacamoleProperty;

/**
 * Configuration options for the extension.
 */
public class Properties {
    static Environment environment = LocalEnvironment.getInstance();

    public static final StringGuacamoleProperty CLOUDEVENTS_SOURCE = new StringGuacamoleProperty() {
        @Override
        public String getName() {
            return "cloudevents-source";
        }
    };

    public static final IntegerGuacamoleProperty WEBSOCKETS_PORT = new IntegerGuacamoleProperty() {
        @Override
        public String getName() {
            return "cloudevents-websocket-port";
        }
    };

    public static final URI getCloudEventsSource() throws GuacamoleException {
        final String source = environment.getProperty(Properties.CLOUDEVENTS_SOURCE, "/cloudevents");
        return URI.create(source);
    }

    public static final Integer getWebSocketPort() throws GuacamoleException {
        return environment.getProperty(Properties.WEBSOCKETS_PORT, 8081);
    }
}
