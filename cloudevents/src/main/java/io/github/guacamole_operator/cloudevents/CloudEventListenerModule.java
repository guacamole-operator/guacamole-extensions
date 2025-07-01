package io.github.guacamole_operator.cloudevents;

import java.util.UUID;

import org.apache.guacamole.GuacamoleException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import io.cloudevents.core.builder.CloudEventBuilder;
import io.github.guacamole_operator.cloudevents.config.Properties;
import io.github.guacamole_operator.cloudevents.sender.CloudEventSender;
import io.github.guacamole_operator.cloudevents.sender.WebSocketCloudEventSender;
import io.github.guacamole_operator.cloudevents.websocket.WebSocketServerImpl;

public class CloudEventListenerModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CloudEventSender.class).to(WebSocketCloudEventSender.class);
    }

    @Provides
    @Singleton
    static WebSocketServerImpl provideWebSocketServerImpl() throws GuacamoleException {
        return new WebSocketServerImpl(Properties.getWebSocketPort());
    }

    @Provides
    static CloudEventBuilder provideCloudEventBuilder() throws GuacamoleException {
        return CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(Properties.getCloudEventsSource());
    }
}
