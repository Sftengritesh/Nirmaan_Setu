package com.nirmaansetu.auth.domain;

public class OtpDeliveryUnavailableException extends RuntimeException {
    public OtpDeliveryUnavailableException() {
        super("OTP delivery is not configured.");
    }
}
