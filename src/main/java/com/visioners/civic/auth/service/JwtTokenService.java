package com.visioners.civic.auth.service;



import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import com.visioners.civic.auth.userdetails.UserPrincipal;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.token.access.expiry}")
    private Long accessTokenExpiry; 

    public String generateToken(UserPrincipal userPrincipal){
        String jti = UUID.randomUUID().toString();
        String role = userPrincipal.getAuthorities()
                    .stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");
        
        String accessToken= Jwts
                            .builder()
                            .setId(jti)
                            .setSubject(userPrincipal.getUsername())
                            .setIssuedAt(new Date())
                            .setExpiration(new Date(System.currentTimeMillis()+accessTokenExpiry))
                            .signWith(getKey())
                            .claim("roles", role)
                            .compact();
        return accessToken;      
    }

    public SecretKey getKey(){
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
