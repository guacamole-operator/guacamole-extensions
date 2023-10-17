package io.github.guacamole_operator.cloudevents;

import java.net.URI;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.environment.Environment;
import org.apache.guacamole.environment.LocalEnvironment;
import org.apache.guacamole.net.event.AuthenticationSuccessEvent;
import org.apache.guacamole.net.event.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;

/**
 * A Listener implementation intended to demonstrate basic use
 * of Guacamole's listener extension API.
 */
public class CloudEventListener implements Listener {
    private WebSocketServerImpl websocket;

    private static final Logger logger = LoggerFactory.getLogger(CloudEventListener.class);
    private Client client;
    private URI source;
    private URI endpoint = null;
    private final CloudEventSender sender = new CloudEventSender();
    private Boolean websocketEnabled = false;

    public CloudEventListener() throws GuacamoleException {
        // Get plugin configuration.
        Environment environment = LocalEnvironment.getInstance();

        String endpointRaw = environment.getProperty(Properties.ENDPOINT);
        source = URI.create(environment.getProperty(Properties.SOURCE, Properties.DEFAULT_SOURCE));
        boolean tlsVerify = environment.getProperty(Properties.TLS_VERIFY, Properties.DEFAULT_TLS_VERIFY);
        websocketEnabled = environment.getProperty(Properties.WEBSOCKETS_ENABLE,
                Properties.DEFAULT_WEBSOCKETS_ENABLE);
        int websocketPort = environment.getProperty(Properties.WEBSOCKETS_PORT, Properties.DEFAULT_WEBSOCKETS_PORT);

        if (endpointRaw != null) {
            endpoint = URI.create(endpointRaw);

            // Build HTTP client.
            client = ClientBuilder.newClient();

            if (!tlsVerify) {
                HostnameVerifier allHostsValid = new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                };

                client = ClientBuilder.newBuilder().hostnameVerifier(allHostsValid).build();
                logger.info("HTTP client for CloudEvents configured with disabled hostname verification!");
            }
        }

        if (endpoint == null && !websocketEnabled) {
            throw new GuacamoleException("Either endpoint or websockets have to be configured!");
        }

        if (websocketEnabled) {
            logger.info("Starting Websocket server.");
            websocket = new WebSocketServerImpl(websocketPort);
            websocket.start();
        }

    }

    @Override
    public void handleEvent(Object event) throws GuacamoleException {
        logger.debug("Received Guacamole event {}", event.getClass().getCanonicalName());

        if (event instanceof AuthenticationSuccessEvent) {
            String user = ((AuthenticationSuccessEvent) event).getAuthenticatedUser()
                    .getCredentials().getUsername();
            io.github.guacamole_operator.cloudevents.events.AuthenticationSuccessEvent e = new io.github.guacamole_operator.cloudevents.events.AuthenticationSuccessEvent();
            e.setUser(user);

            ObjectMapper objectMapper = new ObjectMapper();
            CloudEventData data = PojoCloudEventData.wrap(e, objectMapper::writeValueAsBytes);

            final CloudEvent ce = CloudEventBuilder.v1()
                    .withId(UUID.randomUUID().toString())
                    .withType("io.github.guacamole_operator.authentication.success")
                    .withSource(this.source)
                    .withData(MediaType.APPLICATION_JSON, data)
                    .build();

            try {
                if (endpoint != null) {
                    final WebTarget target = client.target(this.endpoint);
                    sender.sendEventAsStructured(target, ce);
                }

                if (websocketEnabled) {
                    EventFormat format = EventFormatProvider
                            .getInstance()
                            .resolveFormat(JsonFormat.CONTENT_TYPE);

                    // TODO: Check why automatic JSON format registration is not
                    // working. This workaround should not be necessary.
                    if (format == null) {
                        format = new JsonFormat().withForceNonJsonDataToString();
                        EventFormatProvider.getInstance().registerFormat(format);
                    }

                    byte[] serialized = EventFormatProvider
                            .getInstance()
                            .resolveFormat(JsonFormat.CONTENT_TYPE)
                            .serialize(ce);

                    websocket.broadcast(serialized);
                }
            } catch (Exception ex) {
                logger.error("Could not send CloudEvent!", ex);
            }
        }
    }
}
