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

/**
 * A Listener implementation intended to demonstrate basic use
 * of Guacamole's listener extension API.
 */
public class CloudEventListener implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(CloudEventListener.class);
    private Client client;
    private URI endpoint;
    private URI source;

    public CloudEventListener() throws GuacamoleException {
        // Get plugin configuration.
        Environment environment = LocalEnvironment.getInstance();

        endpoint = URI.create(environment.getRequiredProperty(Properties.ENDPOINT));
        source = URI.create(environment.getProperty(Properties.SOURCE, Properties.DEFAULT_SOURCE));
        boolean tlsVerify = environment.getProperty(Properties.TLS_VERIFY, Properties.DEFAULT_TLS_VERIFY);

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

            final WebTarget target = client.target(this.endpoint);

            final CloudEventSender sender = new CloudEventSender();

            try {
                sender.sendEventAsStructured(target, ce);
            } catch (Exception ex) {
                logger.error("Could not send CloudEvent!", ex);
            }
        }
    }
}
