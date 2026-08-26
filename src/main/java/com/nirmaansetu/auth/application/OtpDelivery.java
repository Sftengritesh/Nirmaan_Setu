package com.nirmaansetu.auth.application;

public interface OtpDelivery {
    void deliver(String phoneE164, String code);
}
