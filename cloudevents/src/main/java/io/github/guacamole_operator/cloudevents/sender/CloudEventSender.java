package io.github.guacamole_operator.cloudevents.sender;

import io.cloudevents.CloudEvent;

public interface CloudEventSender {
    public void Send(CloudEvent event);
}
