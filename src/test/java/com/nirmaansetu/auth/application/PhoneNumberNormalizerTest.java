package com.nirmaansetu.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nirmaansetu.auth.domain.AuthFailureException;
import org.junit.jupiter.api.Test;

class PhoneNumberNormalizerTest {
    private final PhoneNumberNormalizer normalizer = new PhoneNumberNormalizer();

    @Test
    void normalizesIndianPhoneNumbersToCanonicalFormat() {
        assertThat(normalizer.normalizeIndian("9876543210")).isEqualTo("+919876543210");
        assertThat(normalizer.normalizeIndian("+919876543210")).isEqualTo("+919876543210");
        assertThat(normalizer.normalizeIndian("+91 98765-43210")).isEqualTo("+919876543210");
        assertThat(normalizer.normalizeIndian("+91-98765-43210")).isEqualTo("+919876543210");
    }

    @Test
    void rejectsInvalidIndianMobileNumbers() {
        // Starts with invalid digit (0-5)
        assertThatThrownBy(() -> normalizer.normalizeIndian("5876543210"))
            .isInstanceOf(AuthFailureException.class);
        assertThatThrownBy(() -> normalizer.normalizeIndian("+915876543210"))
            .isInstanceOf(AuthFailureException.class);

        // Length mismatches (too short or too long)
        assertThatThrownBy(() -> normalizer.normalizeIndian("987654321"))
            .isInstanceOf(AuthFailureException.class);
        assertThatThrownBy(() -> normalizer.normalizeIndian("98765432101"))
            .isInstanceOf(AuthFailureException.class);
        assertThatThrownBy(() -> normalizer.normalizeIndian("+9198765432101"))
            .isInstanceOf(AuthFailureException.class);

        // Empty or null
        assertThatThrownBy(() -> normalizer.normalizeIndian(null))
            .isInstanceOf(AuthFailureException.class);
        assertThatThrownBy(() -> normalizer.normalizeIndian("   "))
            .isInstanceOf(AuthFailureException.class);

        // International numbers
        assertThatThrownBy(() -> normalizer.normalizeIndian("+19876543210"))
            .isInstanceOf(AuthFailureException.class);
        assertThatThrownBy(() -> normalizer.normalizeIndian("+449876543210"))
            .isInstanceOf(AuthFailureException.class);
    }
}
