package io.github.guacamole_operator.cloudevents;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import io.cloudevents.CloudEvent;

public class CloudEventSender {
    public Response sendEventAsStructured(WebTarget target, CloudEvent event) {
        return target
                .request()
                .buildPost(Entity.entity(event, "application/cloudevents+json"))
                .invoke();
    }
}
