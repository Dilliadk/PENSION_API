package com.v1.pension.config;

import java.util.Collections;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class WebsocketJwtAuthInterceptor implements ChannelInterceptor {
  private final JwtTokenUtil jwtUtil;

  public WebsocketJwtAuthInterceptor(JwtTokenUtil jwtTokenUtil) {
    this.jwtUtil = jwtTokenUtil;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    StompCommand command = accessor.getCommand();

    if (StompCommand.CONNECT.equals(command)) {
      var authorizationHeader = accessor.getFirstNativeHeader("Authorization");

      // if not found in Authorization Header, look in queryParameter 'token'
      if (authorizationHeader == null) {
        authorizationHeader = accessor.getFirstNativeHeader("token");
      }

      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
        String token = authorizationHeader.substring(7);
        var jwtPayloadOptional = jwtUtil.parseToken(token);

        if (jwtPayloadOptional.isPresent()) {
          var jwtPayload = jwtPayloadOptional.get();
          if (SecurityContextHolder.getContext().getAuthentication() == null) {
            List<GrantedAuthority> authorities = Collections
                .singletonList(new SimpleGrantedAuthority(jwtPayload.role()));
            var authToken = new UsernamePasswordAuthenticationToken(jwtPayload, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);
          }
        } else {
          throw new MessageDeliveryException("Invalid JWT Token.");
        }
      } else {
        throw new MessageDeliveryException("Missing or invalid 'Authorization' header.");
      }
    }

    return message;
  }
}
