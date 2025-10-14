package com.visioners.civic.auth.service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.visioners.civic.auth.dto.OtpRequest;
import com.visioners.civic.auth.dto.OtpValidationResult;
import com.visioners.civic.auth.dto.OtpVerifyRequest;
import com.visioners.civic.auth.model.OtpPurpose;
import com.visioners.civic.auth.model.OtpData;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final SmsService smsService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.otp.duration}")
    private long otpDuration;

    public String generateOtp(String mobileNumber, OtpPurpose purpose) {
        String otp = String.format("%04d", new SecureRandom().nextInt(999999));

        redisTemplate.opsForValue().set(
                "otp:" + mobileNumber,
                new OtpData(otp, purpose),
                otpDuration, TimeUnit.MINUTES
        );

        return otp;
    }

    public boolean sendOtp(OtpRequest otpRequest) {
        String mobileNumber = otpRequest.mobileNumber();
        OtpPurpose purpose = otpRequest.purpose();

        String otp = generateOtp(mobileNumber, purpose);

        String message = switch (purpose) {
            case PASSWORD_RESET -> "civic\nUse this OTP to reset your password: " + otp +
                    "\nValid for " + otpDuration + " minutes.";
            case VERIFICATION -> "civic\nUse this OTP to verify your mobile number: " + otp +
                    "\nValid for " + otpDuration + " minutes.";
        };
        
        smsService.sendSms(mobileNumber, message);
        return true;
    }

    public OtpValidationResult validateOtp(OtpVerifyRequest request) {
        String key = "otp:" + request.mobileNumber();

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            return new OtpValidationResult(false, "OTP expired. Please request a new one.");
        }

        OtpData storedOtp = (OtpData) redisTemplate.opsForValue().get(key);
        if (storedOtp == null) {
            return new OtpValidationResult(false, "OTP not found.");
        }

        if (storedOtp.purpose() != request.purpose()) {
            return new OtpValidationResult(false, "OTP purpose mismatch.");
        }

        if (!storedOtp.otp().equals(request.otp())) {
            return new OtpValidationResult(false, "Invalid OTP.");
        }

        redisTemplate.delete(key);
        return new OtpValidationResult(true, "Verified successfully.");
    }
}
