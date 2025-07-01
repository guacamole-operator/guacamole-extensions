package io.github.guacamole_operator.cloudevents.sender;

import com.google.inject.Inject;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import io.github.guacamole_operator.cloudevents.websocket.WebSocketServerImpl;

public class WebSocketCloudEventSender implements CloudEventSender {
    @Inject
    private WebSocketServerImpl websocket;

    @Override
    public void Send(CloudEvent event) {
        byte[] serialized = EventFormatProvider
                .getInstance()
                .resolveFormat(JsonFormat.CONTENT_TYPE)
                .serialize(event);

        websocket.broadcast(serialized);
    }
}
