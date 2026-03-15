package com.socratica.config;

import com.socratica.controller.GeminiLiveProxyHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final GeminiLiveProxyHandler geminiLiveProxyHandler;

    public WebSocketConfig(GeminiLiveProxyHandler geminiLiveProxyHandler) {
        this.geminiLiveProxyHandler = geminiLiveProxyHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(geminiLiveProxyHandler, "/api/ai/live/proxy")
                .setAllowedOriginPatterns("*");
    }
}
