package com.v1.pension.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import lombok.RequiredArgsConstructor;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
  private final JwtTokenUtil jwtTokenUtil;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    var bearerToken = request.getHeader("Authorization");
    String jwt = null;

    // extracting jwt: 1. Authorization Header (Mobile) 2. Http-Only Cookie (Web)
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      jwt = bearerToken.substring(7);
    } else if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("jwt_auth_cookie".equals(cookie.getName())) {
          jwt = cookie.getValue();
        }
      }
    }

    if (jwt != null) {
      var jwtPayloadOptional = jwtTokenUtil.parseToken(jwt);
      if (jwtPayloadOptional.isEmpty()) {
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid JWT");
        return;
      }

      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        var jwtPayload = jwtPayloadOptional.get();
        var authorities = Collections.singletonList(new SimpleGrantedAuthority(jwtPayload.role()));

        var authToken = new UsernamePasswordAuthenticationToken(jwtPayload, null, authorities);
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }

  private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    ObjectNode errorResponse = objectMapper.createObjectNode();
    errorResponse.put("timestamp", LocalDateTime.now().toString());
    errorResponse.put("status", status.value());
    errorResponse.put("error", status.getReasonPhrase());
    errorResponse.put("message", message);

    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
