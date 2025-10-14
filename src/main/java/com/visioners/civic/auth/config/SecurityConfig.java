// package com.visioners.civic.auth.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.oauth2.jwt.JwtDecoder;
// import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
// import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
// import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
// import org.springframework.security.web.SecurityFilterChain;

// import com.visioners.civic.auth.service.JwtTokenService;

// import lombok.RequiredArgsConstructor;

// @Configuration
// @EnableWebSecurity
// @RequiredArgsConstructor
// public class SecurityConfig{

//     private final JwtTokenService jwtService;

//     // @Bean
//     // JwtDecoder jwtDecoder(){
//     //     return NimbusJwtDecoder.withSecretKey(jwtService.getKey()).build();
//     // }

//     // @Bean
//     // JwtAuthenticationConverter jwtAuthenticationConverter(){
//     //     JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
//     //     jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
//     //     jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");

//     //     JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//     //     converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        
//     //     return converter;
//     // }

//     @Bean
//     SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
//         return httpSecurity
//             .csrf(csrf -> csrf.disable())
//             .cors(cors -> cors.disable())
//             .authorizeHttpRequests(
//                     request -> request.requestMatchers("/", "/api/auth/**")
//                                 .permitAll()
//                                 .anyRequest()
//                                 .authenticated()
//             )
//             .build();
//     }
// }

package com.visioners.civic.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.visioners.civic.auth.service.JwtAuthenticationFilter;
import com.visioners.civic.auth.service.JwtTokenService;
import org.springframework.security.core.userdetails.UserDetailsService;

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
                        .requestMatchers("/api/auth/**", "/")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // @Bean
    // public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    //     return config.getAuthenticationManager();
    // }
}
