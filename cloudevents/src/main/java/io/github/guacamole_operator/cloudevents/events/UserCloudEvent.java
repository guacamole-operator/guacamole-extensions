package io.github.guacamole_operator.cloudevents.events;

import javax.ws.rs.core.MediaType;

import org.apache.guacamole.net.auth.User;
import org.apache.guacamole.net.event.DirectoryEvent.Operation;
import org.apache.guacamole.net.event.DirectoryFailureEvent;
import org.apache.guacamole.net.event.DirectorySuccessEvent;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;

public class UserCloudEvent extends AbstractUserCloudEvent {
    private final String type;

    public UserCloudEvent(DirectorySuccessEvent<User> event) {
        super(event.getObjectIdentifier());
        String typeBase = "io.github.guacamole_operator.user.success";
        type = String.format("%s.%s", typeBase, mapOperation(event.getOperation()));
    }

    public UserCloudEvent(DirectoryFailureEvent<User> event) {
        super(event.getObjectIdentifier());
        String typeBase = "io.github.guacamole_operator.user.failure";
        type = String.format("%s.%s", typeBase, mapOperation(event.getOperation()));
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

    private String mapOperation(Operation operation) {
        String postfix = new String("unknown");

        switch (operation) {
            case ADD:
                postfix = "create";
                break;
            case GET:
                postfix = "get";
                break;
            case UPDATE:
                postfix = "update";
                break;
            case REMOVE:
                postfix = "delete";
                break;
            default:
                break;
        }

        return postfix;
    }
}
