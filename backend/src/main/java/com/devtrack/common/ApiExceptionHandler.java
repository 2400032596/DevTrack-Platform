package com.devtrack.common;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<ApiError> handleApiException(ApiException exception, HttpServletRequest request) {
    return response(exception.getStatus(), exception.getError(), exception.getMessage(), Map.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      fields.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }
    return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Some fields need attention.", fields);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
    return response(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "A request parameter has an invalid value.", Map.of());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ApiError> handleConflict(DataIntegrityViolationException exception) {
    return response(HttpStatus.CONFLICT, "CONFLICT", "This change conflicts with an existing record.", Map.of());
  }

  private ResponseEntity<ApiError> response(
      HttpStatus status, String error, String message, Map<String, String> fields) {
    return ResponseEntity.status(status)
        .body(new ApiError(status.value(), error, message, Instant.now(), fields));
  }
}
