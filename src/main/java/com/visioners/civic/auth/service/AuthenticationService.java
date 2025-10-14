package com.visioners.civic.auth.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.visioners.civic.auth.dto.LoginRequest;
import com.visioners.civic.auth.dto.LoginResponse;
import com.visioners.civic.auth.dto.OtpVerifyRequest;
import com.visioners.civic.auth.dto.OtpValidationResult;
import com.visioners.civic.auth.dto.RegisterRequest;
import com.visioners.civic.auth.dto.RegisterResponse;
import com.visioners.civic.auth.dto.RegisterSession;
import com.visioners.civic.auth.entity.RefreshToken;
import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.exception.RoleNotFoundException;
import com.visioners.civic.role.entity.Role;
import com.visioners.civic.role.repository.RoleRepository;
import com.visioners.civic.user.entity.Users;
import com.visioners.civic.user.repository.UsersRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import com.visioners.civic.auth.model.OtpPurpose;
import com.visioners.civic.auth.dto.OtpRequest;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UsersRepository usersRepository;
    private final PasswordEncoder bcryptPasswordEncoder;
    private final RoleRepository roleRepository;
    private final JwtTokenService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final OtpService otpService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtTokenService jwtTokenService;
    private final RedisRateLimitService rateLimitService;

    private final String DEFAULT_ROLE = "USER";

    public RegisterResponse register(RegisterRequest registerRequest) {
        String mobileNumber = registerRequest.mobileNumber();

        if (usersRepository.findByMobileNumber(mobileNumber).isPresent()) {
            throw new RuntimeException("Mobile number already registered. Continue with login.");
        }

        String encodedPassword = bcryptPasswordEncoder.encode(registerRequest.password());
        System.out.println("encrypt of " + registerRequest.password() + " is " + encodedPassword);
        // Store in Redis temp session
        RegisterSession session = new RegisterSession(mobileNumber, encodedPassword, DEFAULT_ROLE);
        redisTemplate.opsForValue().set("temp:register:" + mobileNumber, session, 30, TimeUnit.MINUTES);

        // Send OTP
        otpService.sendOtp(new OtpRequest(mobileNumber,OtpPurpose.VERIFICATION));

        return new RegisterResponse(mobileNumber,
                "OTP sent to your mobile. Please verify to complete registration");
    }

    public LoginResponse login(LoginRequest loginRequest) {
        String mobileNumber = loginRequest.mobileNumber();
        String password = loginRequest.password();

        Optional<Users> users = usersRepository.findByMobileNumber(mobileNumber);
        if(!users.get().isVerified()){
            throw new RuntimeException("mobile number not verified");
        }

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(mobileNumber, password);

        Authentication authentication = authenticationManager.authenticate(token);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Users user = userPrincipal.getUser();

        String accessToken = jwtService.generateToken(userPrincipal);
        RefreshToken refreshToken = refreshTokenService.createToken(user);

        return LoginResponse.builder()
                .mobileNumber(mobileNumber)
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .timestamp(Instant.now())
                .build();
    }

    public ResponseEntity<Map<String, Object>> verifyOtp(OtpVerifyRequest request) {
        OtpValidationResult otpValidation = otpService.validateOtp(request);

        if (!otpValidation.valid()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "verified", false,
                    "message", otpValidation.message()
            ));
        }

        if (request.purpose() == OtpPurpose.VERIFICATION) {
            if (!isRegisterSessionValid(request.mobileNumber())) {
                return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                        .body(Map.of("message", "Session expired. Please register again."));
            }

            saveNewUser(request.mobileNumber());
            redisTemplate.delete("temp:register:" + request.mobileNumber());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "User registered successfully. Continue to login."));
        }
        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }

    public ResponseEntity<LoginResponse> refresh(Map<String, String> body){
        String refreshTokenStr = body.get("refreshToken");

        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr)
                .orElse(null);

        if (refreshToken == null || !refreshTokenService.isValid(refreshTokenStr)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Users user = refreshToken.getUser();
        String accessToken = jwtTokenService.generateToken(new UserPrincipal(user));

        return ResponseEntity.ok(LoginResponse.builder()
                .mobileNumber(user.getMobileNumber())
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .timestamp(Instant.now())
                .build());
    }

    public ResponseEntity<Map<String, Object>> requestOtp(OtpRequest request, HttpServletRequest servletRequest) {
        String clientIp = getClientIp(servletRequest);

        if (!rateLimitService.isAllowed(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("success", false, "message", "Too many OTP requests from this IP. Try again later.", "timestamp", Instant.now()));
        }

        if (rateLimitService.isInCooldown(clientIp)) {
            long cooldown = rateLimitService.getCooldownRemaining(clientIp);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                    "success", false,
                    "cooldown_remaining", cooldown,
                    "message", "Please wait " + cooldown + " seconds before requesting a new OTP."
                ));
        }

        //for future use 
        if (request.purpose() == OtpPurpose.PASSWORD_RESET && !mobileNumberExists(request.mobileNumber())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Mobile number not registered", "timestamp", Instant.now()));
        }

        if (request.purpose() == OtpPurpose.VERIFICATION && mobileNumberExists(request.mobileNumber())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "The mobile number is already verified", "timestamp", Instant.now()));
        }

        if (otpService.sendOtp(request)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "OTP sent successfully to your number", "timestamp", Instant.now()));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Failed to send OTP. Please try again later.", "timestamp", Instant.now()));
        }
    }

    //utility functions 
    private boolean isRegisterSessionValid(String mobileNumber) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("temp:register:" + mobileNumber));
    }

    private void saveNewUser(String mobileNumber) {
        RegisterSession session =
                (RegisterSession) redisTemplate.opsForValue().get("temp:register:" + mobileNumber);

        if (session != null) {
            Role role = roleRepository.findByName(session.role())
                .orElseThrow(() -> new RoleNotFoundException("Role: " + DEFAULT_ROLE + " not found"));

            Users user = new Users();
            user.setMobileNumber(session.mobileNumber());
            user.setPassword(session.encodedPassword());
            user.setRole(role);
            user.setVerified(true);
            usersRepository.save(user);
        }
    }

    private boolean mobileNumberExists(String mobileNumber){
        return usersRepository.existsByMobileNumber(mobileNumber);
    }

    private String getClientIp(HttpServletRequest servletRequest) {
        String header = servletRequest.getHeader("X-Forwarded-For");

        if(header != null)
            return header.split(", ")[0];
        
        return servletRequest.getRemoteAddr();
    }
}
