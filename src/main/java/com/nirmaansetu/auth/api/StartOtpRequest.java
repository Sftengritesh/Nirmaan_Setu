package com.nirmaansetu.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartOtpRequest(@NotBlank @Size(max = 30) String phoneNumber) { }
