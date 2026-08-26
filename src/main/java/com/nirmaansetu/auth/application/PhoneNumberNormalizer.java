package com.nirmaansetu.auth.application;

import com.nirmaansetu.auth.domain.AuthFailureException;
import org.springframework.stereotype.Component;

@Component
public class PhoneNumberNormalizer {
    public String normalizeIndian(String input) {
        if (input == null) throw new AuthFailureException("Invalid phone number.");
        String compact = input.replaceAll("[\\s()\\-]", "");
        String national;
        if (compact.startsWith("+91")) national = compact.substring(3);
        else if (compact.startsWith("91") && compact.length() == 12) national = compact.substring(2);
        else national = compact;
        if (!national.matches("[6-9][0-9]{9}")) throw new AuthFailureException("Invalid phone number.");
        return "+91" + national;
    }
}
