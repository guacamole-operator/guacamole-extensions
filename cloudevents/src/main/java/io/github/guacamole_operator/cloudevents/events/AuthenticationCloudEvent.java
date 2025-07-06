package io.github.guacamole_operator.cloudevents.events;

import javax.ws.rs.core.MediaType;

import org.apache.guacamole.net.event.AuthenticationFailureEvent;
import org.apache.guacamole.net.event.AuthenticationSuccessEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;

public class AuthenticationCloudEvent extends AbstractUserCloudEvent {
    private final String type;

    public AuthenticationCloudEvent(AuthenticationSuccessEvent event) {
        super(event.getCredentials().getUsername());
        type = "io.github.guacamole_operator.authentication.success";
    }

    public AuthenticationCloudEvent(AuthenticationFailureEvent event) {
        super(event.getCredentials().getUsername());
        type = "io.github.guacamole_operator.authentication.failure";
    }

    public CloudEvent getCloudEvent(CloudEventBuilder builder) {
        final ObjectMapper objectMapper = new ObjectMapper();
        CloudEventData data = PojoCloudEventData.wrap(this, objectMapper::writeValueAsBytes);

        CloudEvent event = builder
                .withType(this.type)
                .withData(MediaType.APPLICATION_JSON, data)
                .build();

        return event;
    }
}
