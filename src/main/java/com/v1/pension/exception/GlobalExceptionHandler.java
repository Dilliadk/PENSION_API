package com.v1.pension.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationExceptions(
      MethodArgumentNotValidException ex, WebRequest request) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    var output = Map.of("timestamp", LocalDateTime.now(), "message", "validation error", "errors", errors);
    return new ResponseEntity<>(output, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorDetails> handleDataIntegrityException(DataIntegrityViolationException ex,
      WebRequest request) {
    var msg = "SQL Error: Data Integrity Violation";
    logger.error(msg, ex);
    return new ResponseEntity<>(generateErrorDetails(msg, request), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorDetails> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
    return new ResponseEntity<>(generateErrorDetails(ex.getMessage(), request), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorDetails> handleBadRequestException(BadRequestException ex, WebRequest request) {
    return new ResponseEntity<>(generateErrorDetails(ex.getMessage(), request), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ErrorDetails> handleAuthorizationDeniedException(AuthorizationDeniedException ex,
      WebRequest request) {
    return new ResponseEntity<>(generateErrorDetails(ex.getMessage(), request), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorDetails> handleGenericException(Exception ex, WebRequest request) {
    var msg = ex.getMessage();
    return new ResponseEntity<>(generateErrorDetails(msg, request), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorDetails generateErrorDetails(String message, WebRequest request) {
    return new ErrorDetails(LocalDateTime.now(), message, request.getDescription(false));
  }
}
