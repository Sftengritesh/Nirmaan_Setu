package com.nirmaansetu.team.api;

import com.nirmaansetu.team.domain.TeamException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice(assignableTypes = TeamController.class)
public class TeamExceptionHandler {

    @ExceptionHandler(TeamException.class)
    ResponseEntity<Map<String, String>> handleTeamException(TeamException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "TEAM_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request data.");
    }

        /** Re-throw security exceptions so Spring Security handles 401/403 correctly. */
    @ExceptionHandler({AccessDeniedException.class, AuthenticationException.class})
    ResponseEntity<?> handleSecurity(RuntimeException ex) throws RuntimeException {
        throw ex;
    }
@ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Request could not be completed.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
