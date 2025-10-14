package com.visioners.civic.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {

    @Value("${twilio.phoneNumber}")
    private String fromNumber;

    public void sendSms(String toNumber, String message) {
        try {
            Message sentMessage = Message.creator(
                new PhoneNumber(normalizeIndianNumber(toNumber)),
                new PhoneNumber(fromNumber),
                message
            ).create();
            System.out.println("Message SID: " + sentMessage.getSid());
        } catch (Exception e) {
            throw new RuntimeException("Unable to send SMS", e);
        }
    }
    
    private String normalizeIndianNumber(String number) {
        if (number.startsWith("+91")) return number;
        return "+91" + number;
    }
}
