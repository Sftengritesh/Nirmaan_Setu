package com.nirmaansetu.auth.application;

import com.nirmaansetu.auth.domain.OtpDeliveryUnavailableException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class UnavailableOtpDelivery implements OtpDelivery {
    @Override
    public void deliver(String phoneE164, String code) {
        throw new OtpDeliveryUnavailableException();
    }
}
