package io.github.guacamole_operator.cloudevents.events;

public abstract class AbstractUserCloudEvent {
    private String username;

    public AbstractUserCloudEvent(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
