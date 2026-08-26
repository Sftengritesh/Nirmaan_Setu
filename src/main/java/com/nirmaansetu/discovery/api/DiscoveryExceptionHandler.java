package com.nirmaansetu.discovery.api;

import com.nirmaansetu.discovery.domain.DiscoveryException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DiscoveryController.class)
public class DiscoveryExceptionHandler {

    @ExceptionHandler(DiscoveryException.class)
    ResponseEntity<Map<String, String>> handleDiscoveryException(DiscoveryException ex) {
        return error(HttpStatus.BAD_REQUEST, "DISCOVERY_ERROR", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Discovery request failed.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
