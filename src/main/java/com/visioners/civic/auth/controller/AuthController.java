package com.visioners.civic.auth.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.visioners.civic.auth.dto.LoginRequest;
import com.visioners.civic.auth.dto.LoginResponse;
import com.visioners.civic.auth.dto.OtpVerifyRequest;
import com.visioners.civic.auth.dto.RegisterRequest;
import com.visioners.civic.auth.dto.RegisterResponse;
import com.visioners.civic.auth.service.AuthenticationService;
import com.visioners.civic.auth.service.RefreshTokenService;
import com.visioners.civic.auth.dto.OtpRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        RegisterResponse registerResponse = authenticationService.register(registerRequest);
        return ResponseEntity.ok(registerResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authenticationService.login(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody Map<String, String> body) {
        return authenticationService.refresh(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        String refreshTokenStr = body.get("refreshToken");
        refreshTokenService.deleteToken(refreshTokenStr);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verifyOtp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequest verifyRequest) {
        return authenticationService.verifyOtp(verifyRequest);
    }

    @PostMapping("/requestOtp")
    public ResponseEntity<Map<String, Object>> requestOtp(
        @Valid @RequestBody 
        OtpRequest request,
        HttpServletRequest servletRequest
    ) {
        return authenticationService.requestOtp(request, servletRequest);
    }
}
