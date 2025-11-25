// package com.visioners.civic.util;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.scheduling.annotation.Async;
// import org.springframework.stereotype.Service;
// import com.twilio.exception.ApiException;
// import com.twilio.rest.api.v2010.account.Message;
// import com.twilio.type.PhoneNumber;

// @Service
// public class SmsService {

//     private static final Logger log = LoggerFactory.getLogger(SmsService.class);

//     @Value("${twilio.phoneNumber}")
//     private String fromNumber;
    
//     @Async("asyncExecutor")
//     public void sendSms(String toNumber, String message) {
//         try {
//             Message.creator(
//                 new PhoneNumber(normalizeIndianNumber(toNumber)),
//                 new PhoneNumber(fromNumber),
//                 message
//             ).create();
//             log.debug("SMS sent to {} from {}", normalizeIndianNumber(toNumber), fromNumber);
            
//         } catch (Exception e) {
//             // If Twilio ApiException, log detailed info for diagnosis
//             if (e instanceof ApiException) {
//                 ApiException ae = (ApiException) e;
//                 log.error("SMS sending failed: Twilio ApiException code={} status={} message={}",
//                         ae.getCode(), ae.getStatusCode(), ae.getLocalizedMessage());
//             } else {
//                 log.error("SMS sending failed: {}", e.getMessage(), e);
//             }
//             throw new RuntimeException("Unable to send SMS", e);
//         }
//     }
    
//     private String normalizeIndianNumber(String number) {
//         if (number.startsWith("+91")) return number;
//         return "+91" + number;
//     }
// }

// ...existing code...
// ...existing code...
package com.visioners.civic.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String fromNumber;

    @PostConstruct
    private void init() {
        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            throw new IllegalStateException("Twilio credentials are not set (twilio.accountSid / twilio.authToken).");
        }
        Twilio.init(accountSid.trim(), authToken.trim());
    }

    public void sendSms(String to, String body) {
        try {
            Message.creator(
                    new PhoneNumber(normalizeIndianNumber(to)),
                    new PhoneNumber(fromNumber),
                    body
            ).create();
        } catch (ApiException e) {
            throw new RuntimeException("Unable to send SMS", e);
        }
    }

    private String normalizeIndianNumber(String number) {
        if (number.startsWith("+91")) return number;
        return "+91" + number;
    }
}
// ...existing code...
