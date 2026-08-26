package com.nirmaansetu.auth.api;

import com.nirmaansetu.auth.domain.AuthFailureException;
import com.nirmaansetu.auth.domain.OtpDeliveryUnavailableException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthExceptionHandler {
    @ExceptionHandler(AuthFailureException.class)
    ResponseEntity<Map<String, String>> authenticationFailure(AuthFailureException exception) {
        return error(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", exception.getMessage());
    }
    @ExceptionHandler(OtpDeliveryUnavailableException.class)
    ResponseEntity<Map<String, String>> deliveryUnavailable(OtpDeliveryUnavailableException exception) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "AUTHENTICATION_UNAVAILABLE", "Authentication is temporarily unavailable.");
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalidRequest(MethodArgumentNotValidException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_AUTH_REQUEST", "Invalid authentication request.");
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> handleGenericException(Exception exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Authentication could not be completed.");
    }
    private ResponseEntity<Map<String, String>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
