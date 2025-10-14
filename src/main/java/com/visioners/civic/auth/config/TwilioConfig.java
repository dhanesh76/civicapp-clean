package com.visioners.civic.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.twilio.Twilio;

import jakarta.annotation.PostConstruct;


@Configuration
class TwilioConfig{

    @Value("${twilio.accountSid}")
    private String accountSsid;

    @Value("${twilio.authToken}")
    private String authToken;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSsid, authToken);
    }
}