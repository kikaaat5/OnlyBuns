package com.example.OnlyBuns.config;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class WebSocketSessionListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSessionListener.class);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : "anonymous";
        logger.info("WEBSOCKET CONNECTED: Session ID={}, User={}", event.getMessage().getHeaders().get("simpSessionId"), username);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String username = event.getUser() != null ? event.getUser().getName() : "anonymous";
        logger.info("WEBSOCKET DISCONNECTED: Session ID={}, User={}", event.getMessage().getHeaders().get("simpSessionId"), username);
    }
}
