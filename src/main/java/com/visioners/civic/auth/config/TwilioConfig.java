package com.visioners.civic.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
class TwilioConfig{

    private static final Logger log = LoggerFactory.getLogger(TwilioConfig.class);

    @Value("${twilio.accountSid}")
    private String accountSsid;

    @Value("${twilio.authToken}")
    private String authToken;

    @PostConstruct
    public void initTwilio() {
        // Initialize Twilio SDK with provided credentials
        Twilio.init(accountSsid, authToken);
        // Log initialization (mask majority of account SID)
        if (accountSsid != null && accountSsid.length() > 6) {
            String masked = "***" + accountSsid.substring(accountSsid.length() - 6);
            log.info("Twilio initialized for account {}", masked);
        } else {
            log.info("Twilio initialized (account SID length unexpected)");
            log.debug("Twilio initialized with account SID: {}", accountSsid);
            log.debug("Twilio initialized with auth token: {}", authToken);
        }
    }
}