package io.github.guacamole_operator.cloudevents;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.event.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import io.github.guacamole_operator.cloudevents.websocket.WebSocketServerImpl;

/**
 * Listener implementation for sending out Guacamole events in the CloudEvents
 * format.
 */
public class CloudEventListener implements Listener {
    private static final Logger logger = LoggerFactory.getLogger(CloudEventListener.class);

    private final Injector injector;

    public CloudEventListener() throws GuacamoleException {
        injector = Guice.createInjector(new CloudEventListenerModule());

        // Register CloudEvent event formats. This should usually not be necessary,
        // but the library's class loading mechanism seemingly does not work in
        // the extension environment and/or has problems with the packaging in general.
        EventFormat jsonFormat = new JsonFormat().withForceNonJsonDataToString();
        EventFormatProvider.getInstance().registerFormat(jsonFormat);

        logger.info("Starting WebSocket server.");
        injector.getInstance(WebSocketServerImpl.class).start();
    }

    @Override
    public void handleEvent(Object event) throws GuacamoleException {
        logger.debug("Received Guacamole event {}", event);
        injector.getInstance(CloudEventHandler.class).handle(event);
    }
}
