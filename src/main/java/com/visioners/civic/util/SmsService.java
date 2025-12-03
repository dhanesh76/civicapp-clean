package com.visioners.civic.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.phoneNumber}")
    private String fromNumber;
    
    @Async("asyncExecutor")
    public void sendSms(String toNumber, String message) {
        try {
            Message.creator(
                new PhoneNumber(normalizeIndianNumber(toNumber)),
                new PhoneNumber(fromNumber),
                message
            ).create();
            log.debug("SMS sent to {} from {}", normalizeIndianNumber(toNumber), fromNumber);
            
        } catch (Exception e) {
            // If Twilio ApiException, log detailed info for diagnosis
            if (e instanceof ApiException) {
                ApiException ae = (ApiException) e;
                log.error("SMS sending failed: Twilio ApiException code={} status={} message={}",
                        ae.getCode(), ae.getStatusCode(), ae.getLocalizedMessage());
            } else {
                log.error("SMS sending failed: {}", e.getMessage(), e);
            }
            throw new com.visioners.civic.exception.ExternalServiceException("Unable to send SMS", e);
        }
    }
    
    private String normalizeIndianNumber(String number) {
        if (number.startsWith("+91")) return number;
        return "+91" + number;
    }
}
