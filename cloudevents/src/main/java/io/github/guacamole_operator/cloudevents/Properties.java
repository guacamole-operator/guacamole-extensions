package io.github.guacamole_operator.cloudevents;

import org.apache.guacamole.properties.BooleanGuacamoleProperty;
import org.apache.guacamole.properties.StringGuacamoleProperty;

public class Properties {
    public static final StringGuacamoleProperty ENDPOINT = new StringGuacamoleProperty() {
        @Override
        public String getName() {
            return "cloudevents-endpoint";
        }
    };

    public static final StringGuacamoleProperty SOURCE = new StringGuacamoleProperty() {
        @Override
        public String getName() {
            return "cloudevents-source";
        }
    };

    public static final BooleanGuacamoleProperty TLS_VERIFY = new BooleanGuacamoleProperty() {
        @Override
        public String getName() {
            return "cloudevents-tls-verify";
        }
    };

    public static final String DEFAULT_SOURCE = "/cloudevents";
    public static final boolean DEFAULT_TLS_VERIFY = true;
}
