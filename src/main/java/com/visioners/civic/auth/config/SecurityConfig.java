package com.visioners.civic.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.visioners.civic.auth.service.JwtAuthenticationFilter;
import com.visioners.civic.auth.service.JwtTokenService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthFilter() {
        return new JwtAuthenticationFilter(jwtTokenService, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/").permitAll()
                        .requestMatchers("/api/departments/complaints/**").hasRole("OFFICER")
                        .requestMatchers("/api/workers/complaints/**").hasRole("FIELD_WORKER")
                        .requestMatchers("/api/users/complaints/**").hasRole("USER")
                        .requestMatchers("/ws/**").permitAll()        // allow websocket handshake
                        .requestMatchers("/topic/**").permitAll()     // allow broker messages
                        .requestMatchers("/app/**").permitAll()       // stomp app prefix
                        .requestMatchers("/ws-test/index.html").permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
