package com.visioners.civic.auth.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.visioners.civic.auth.entity.RefreshToken;
import com.visioners.civic.auth.repository.RefreshTokenRepository;
import com.visioners.civic.user.entity.Users;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.token.refresh.expiry}") 
    private Long refreshTokenExpiry;

    public RefreshToken createToken(Users user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(getRefreshToken());
        token.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiry));

        return refreshTokenRepository.save(token);
    }

    public boolean isValid(String tokenStr) {
        boolean valid = refreshTokenRepository.findByToken(tokenStr)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
        if(!valid) deleteToken(tokenStr);
        return valid;
    }

    @Transactional
    public void deleteToken(String tokenStr) {
        refreshTokenRepository.deleteByToken(tokenStr);
    }

    @Transactional
    public void deleteAllTokensForUser(Users user) {
        refreshTokenRepository.deleteAllByUser(user);
    }
        
    private String getRefreshToken() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return  Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public Optional<RefreshToken> findByToken(String refreshToken){
        return refreshTokenRepository.findByToken(refreshToken);
    }
}
