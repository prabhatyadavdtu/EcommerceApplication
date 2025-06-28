package com.example.ecommerceapplication.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class TwilioSmsService implements SmsService {

    private static final Logger logger = LoggerFactory.getLogger(TwilioSmsService.class);

    @Value("${twilio.account-sid}")
    private String ACCOUNT_SID;

    @Value("${twilio.auth-token}")
    private String AUTH_TOKEN;

    @Value("${twilio.phone-number}")
    private String FROM_NUMBER;

    @Override
    public boolean sendSms(String phoneNumber, String message) {
        try {
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(FROM_NUMBER),
                    message
            ).create();     
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS", e);
            return false;
        }
    }
}