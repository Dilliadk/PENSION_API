package com.v1.pension.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
  private final WebsocketJwtAuthInterceptor webSocketJwtAuthInterceptor;

  public WebsocketConfig(WebsocketJwtAuthInterceptor interceptor) {
    this.webSocketJwtAuthInterceptor = interceptor;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app"); // to trigger websocket method, client need this prefix
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOriginPatterns("https://demoipms.fcgo.gov.np", "http://localhost:4200", "https://ipmsv2.fcgo.gov.np")
        .setAllowedOrigins("https://demoipms.fcgo.gov.np", "http://localhost:4200", "https://ipmsv2.fcgo.gov.np");
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(webSocketJwtAuthInterceptor);
  }
}
