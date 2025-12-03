package com.visioners.civic.auth.config;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.visioners.civic.auth.service.JwtTokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    @Nullable
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        try {
            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                List<String> authHeaders = accessor.getNativeHeader("Authorization");

                if (authHeaders == null || authHeaders.isEmpty()) {
                    throw new IllegalArgumentException("Missing Authorization header in STOMP CONNECT");
                }

                String rawHeader = authHeaders.get(0);
                if (rawHeader == null || !rawHeader.startsWith("Bearer ")) {
                    throw new IllegalArgumentException("Invalid Authorization header format");
                }

                String token = rawHeader.substring(7);

                String username = jwtTokenService.extractUsername(token);

                if (username == null) {
                    throw new IllegalArgumentException("JWT token has no subject");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (!jwtTokenService.validateToken(token, userDetails)) {
                    throw new IllegalArgumentException("Invalid or expired JWT token");
                }
                UsernamePasswordAuthenticationToken authenticationToken = UsernamePasswordAuthenticationToken.authenticated(userDetails, null, userDetails.getAuthorities());

                accessor.setUser(authenticationToken);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("WebSocket authentication failed: " + ex.getMessage());
        }
        return message;
    }

}