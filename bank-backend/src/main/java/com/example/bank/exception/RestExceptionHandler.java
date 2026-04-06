package com.example.bank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String errorMessage = translateValidationMessage(error.getDefaultMessage());
            if (error instanceof FieldError fieldError) {
                validationErrors.put(fieldError.getField(), errorMessage);
                return;
            }
            if (error instanceof ObjectError objectError) {
                validationErrors.put(objectError.getObjectName(), errorMessage);
            }
        });
        String summary = new LinkedHashSet<>(validationErrors.values()).stream()
                .collect(Collectors.joining(" "));
        String message = summary.isBlank() ? "Невалидни входни данни" : summary;
        return buildError(HttpStatus.BAD_REQUEST, message, request, validationErrors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Невалиден формат на параметър: " + ex.getName(), request, null);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Невалиден формат на заявката", request, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> validationErrors.put(v.getPropertyPath().toString(), v.getMessage()));
        return buildError(HttpStatus.BAD_REQUEST, "Невалидни параметри", request, validationErrors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Вътрешна грешка в сървъра", request, null);
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status,
                                                           String message,
                                                           HttpServletRequest request,
                                                           Map<String, String> validationErrors) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", message);
        body.put("path", request != null ? request.getRequestURI() : "");
        if (validationErrors != null && !validationErrors.isEmpty()) {
            body.put("validationErrors", validationErrors);
        }
        return ResponseEntity.status(status).body(body);
    }

    private String translateValidationMessage(String msg) {
        if (msg == null) {
            return "Невалидна стойност";
        }
        if (msg.contains("must not be null") || msg.contains("must not be blank")) {
            return "Полето е задължително";
        }
        if (msg.contains("size must be between 10 and 10")) {
            return "Стойността трябва да е точно 10 символа";
        }
        if (msg.contains("size must be between 9 and 13")) {
            return "Стойността трябва да е между 9 и 13 символа";
        }
        if (msg.contains("must be greater than 0")) {
            return "Стойността трябва да е по-голяма от 0";
        }
        if (msg.contains("must be greater than or equal to 0")) {
            return "Стойността не може да е отрицателна";
        }
        return msg;
    }
}

