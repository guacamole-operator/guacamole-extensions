package io.github.guacamole_operator.cloudevents.events;

public class AuthenticationSuccessEvent {
    private String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
