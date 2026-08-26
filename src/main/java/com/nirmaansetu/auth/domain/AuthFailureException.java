package com.nirmaansetu.auth.domain;

public class AuthFailureException extends RuntimeException {
    public AuthFailureException(String message) {
        super(message);
    }
}
