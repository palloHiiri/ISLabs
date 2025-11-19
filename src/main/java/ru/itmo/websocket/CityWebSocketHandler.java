package ru.itmo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class CityWebSocketHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CityWebSocketHandler() {
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Подключили вебсокет");
        sessions.add(session);
        System.out.println("WebSocket соединение установлено: " + session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket ошибка транспорта: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        System.out.println("WebSocket соединение закрыто: " + session.getId());
    }

    public void broadcastUpdate(String type, Object data) {
        System.out.println("broadcastUpdate: " + type + " " + data);

        if (sessions.isEmpty()) return;

        try {
            WebSocketMessage message = new WebSocketMessage(type, data);
            String jsonMessage = objectMapper.writeValueAsString(message);
            TextMessage textMessage = new TextMessage(jsonMessage);

            sessions.removeIf(session -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                        System.out.println("Отправлено сообщение WebSocket: " + jsonMessage);
                        return false;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка отправки WebSocket сообщения: " + e.getMessage());
                }
                return true;
            });
        } catch (Exception e) {
            System.err.println("Ошибка создания WebSocket сообщения: " + e.getMessage());
        }
    }
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WebSocketMessage {
        private String type;
        private Object data;

    }
}
