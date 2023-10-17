package io.github.guacamole_operator.cloudevents;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerImpl extends WebSocketServer {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerImpl.class);
    private final int port;

    public WebSocketServerImpl(int port) {
        super(new InetSocketAddress(port));
        this.port = port;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("New WebSocket connection to {}.", conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("WebSocket connection closed for {} with exit code {}. Additional info: {}.",
                conn.getRemoteSocketAddress(), code, reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        // Nothing to do.
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        // Nothing to do.
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.error("an error occurred on connection {}.", conn.getRemoteSocketAddress(), ex);
    }

    @Override
    public void onStart() {
        logger.info("WebSocket server started on :{}.", this.port);
    }
}
