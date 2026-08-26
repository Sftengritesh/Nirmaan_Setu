package com.nirmaansetu.project.api;

import com.nirmaansetu.project.domain.ProjectException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = ProjectController.class)
public class ProjectExceptionHandler {

    @ExceptionHandler(ProjectException.class)
    ResponseEntity<Map<String, String>> handleProjectException(ProjectException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "PROJECT_ERROR", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request data.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Request could not be completed.");
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of("code", code, "message", message));
    }
}
