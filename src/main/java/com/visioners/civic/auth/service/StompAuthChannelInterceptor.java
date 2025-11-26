package com.visioners.civic.auth.service;

import java.util.Date;
import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> nativeAuth = accessor.getNativeHeader("Authorization");
            String token = null;

            if (nativeAuth != null && !nativeAuth.isEmpty()) {
                String raw = nativeAuth.get(0);
                if (raw != null && raw.startsWith("Bearer ")) {
                    token = raw.substring(7);
                }
            }

            if (token == null) {
                // No token provided -> reject connection
                throw new IllegalArgumentException("Missing Authorization Bearer token in STOMP CONNECT headers");
            }

            try {
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(jwtService.getKey())
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                String username = claims.getSubject();
                Date exp = claims.getExpiration();

                if (username == null || exp == null || exp.before(new Date())) {
                    throw new IllegalArgumentException("Invalid or expired JWT token");
                }

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Attach authenticated principal to the WebSocket session
                accessor.setUser(principal);

            } catch (Exception ex) {
                throw new IllegalArgumentException("Invalid JWT token: " + ex.getMessage());
            }
        }

        return message;
    }
}
