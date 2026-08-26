package com.nirmaansetu.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(@NotBlank @Size(max = 30) String phoneNumber,
                               @NotBlank @Pattern(regexp = "[0-9]{6}") String otp) { }
