package com.socratica.config;

import com.socratica.controller.GeminiLiveProxyHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

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

    /** Raise the WebSocket message size limit to 10 MB to handle large Gemini audio frames. */
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(10 * 1024 * 1024);
        container.setMaxBinaryMessageBufferSize(10 * 1024 * 1024);
        return container;
    }
}
