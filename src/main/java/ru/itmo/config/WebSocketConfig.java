package ru.itmo.config;

import ru.itmo.websocket.CityWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CityWebSocketHandler cityWebSocketHandler;

    public WebSocketConfig(CityWebSocketHandler cityWebSocketHandler) {
        this.cityWebSocketHandler = cityWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(cityWebSocketHandler, "/ws/*")
                .setAllowedOrigins("*");
    }


}
