package com.nirmaansetu.auth.application;

import com.nirmaansetu.auth.domain.OtpDeliveryUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class UnavailableOtpDelivery implements OtpDelivery {
    private static final Logger log = LoggerFactory.getLogger(UnavailableOtpDelivery.class);
    private final boolean devMode;

    public UnavailableOtpDelivery(@Value("${auth.otp.dev-mode:true}") boolean devMode) {
        this.devMode = devMode;
    }

    @Override
    public void deliver(String phoneE164, String code) {
        if (!devMode) {
            throw new OtpDeliveryUnavailableException();
        }
        log.info("\n========================================\n[DEV-ONLY] OTP CODE FOR {}: {}\n========================================\n", phoneE164, code);
    }
}
