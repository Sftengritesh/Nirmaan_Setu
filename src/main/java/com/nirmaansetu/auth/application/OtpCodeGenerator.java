package com.nirmaansetu.auth.application;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class OtpCodeGenerator {
    private final SecureRandom secureRandom = new SecureRandom();
    public String generate() { return String.format("%06d", secureRandom.nextInt(1_000_000)); }
}
