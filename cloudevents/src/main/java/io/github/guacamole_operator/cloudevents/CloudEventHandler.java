package io.github.guacamole_operator.cloudevents;

import org.apache.guacamole.net.auth.Directory;
import org.apache.guacamole.net.auth.User;
import org.apache.guacamole.net.event.AuthenticationFailureEvent;
import org.apache.guacamole.net.event.AuthenticationSuccessEvent;
import org.apache.guacamole.net.event.DirectoryFailureEvent;
import org.apache.guacamole.net.event.DirectorySuccessEvent;

import com.google.inject.Inject;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.github.guacamole_operator.cloudevents.events.AuthenticationCloudEvent;
import io.github.guacamole_operator.cloudevents.events.UserCloudEvent;
import io.github.guacamole_operator.cloudevents.sender.CloudEventSender;

public class CloudEventHandler {
    @Inject
    CloudEventBuilder builder;

    @Inject
    private CloudEventSender sender;

    public void handle(Object object) {
        // Authentication events.
        if (object instanceof AuthenticationSuccessEvent) {
            AuthenticationCloudEvent ce = new AuthenticationCloudEvent((AuthenticationSuccessEvent) object);

            CloudEvent event = ce.getCloudEvent(builder);
            sender.Send(event);

            return;
        }

        if (object instanceof AuthenticationFailureEvent) {
            AuthenticationCloudEvent ce = new AuthenticationCloudEvent((AuthenticationFailureEvent) object);

            CloudEvent event = ce.getCloudEvent(builder);
            sender.Send(event);

            return;
        }

        // User events.
        if (object instanceof DirectorySuccessEvent) {
            if (isUserSuccessEvent((DirectorySuccessEvent<?>) object)) {
                @SuppressWarnings("unchecked")
                UserCloudEvent ce = new UserCloudEvent((DirectorySuccessEvent<User>) object);

                CloudEvent event = ce.getCloudEvent(builder);
                sender.Send(event);

                return;
            }
        }

        if (object instanceof DirectoryFailureEvent) {
            if (isUserFailureEvent((DirectoryFailureEvent<?>) object)) {
                @SuppressWarnings("unchecked")
                UserCloudEvent ce = new UserCloudEvent((DirectoryFailureEvent<User>) object);

                CloudEvent event = ce.getCloudEvent(builder);
                sender.Send(event);

                return;
            }
        }
    }

    private Boolean isUserSuccessEvent(DirectorySuccessEvent<?> event) {
        return event.getDirectoryType() == Directory.Type.USER;
    }

    private Boolean isUserFailureEvent(DirectoryFailureEvent<?> event) {
        return event.getDirectoryType() == Directory.Type.USER;
    }
}
