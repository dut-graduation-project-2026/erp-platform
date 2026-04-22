package com.dut.erp.exception;

import com.dut.erp.dto.response.ErrorResponse;
import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // ============ Security Exceptions ============
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    return buildResponse(ErrorCode.UNAUTHORIZED);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
    return buildResponse(ErrorCode.FORBIDDEN);
  }

  // ============ Validation Exceptions ============
  @ExceptionHandler({MethodArgumentNotValidException.class})
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    log.warn("Method argument not valid: {}", ex.getMessage());
    return handleValidationError(ex.getBindingResult());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return buildResponse(ErrorCode.VALIDATION_FAILED, ex.getMessage(), null);
  }

  // ============ Not Found Exceptions ============
  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return buildResponse(ErrorCode.RESOURCE_NOT_FOUND);
  }

  // =========== Method Not Allowed ============
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("Method not allowed: {}", ex.getMessage());
    return buildResponse(ErrorCode.METHOD_NOT_ALLOWED);
  }

  // ============ Business Exceptions ============
  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BaseException ex) {
    log.warn("Business error: {}", ex.getMessage());
    return buildResponse(ex.getErrorCode(), ex.getMessage(), ex.getDetails());
  }

  // ============ Unexpected Exceptions ============
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
    log.error("Unexpected error", ex);
    return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR);
  }

  // ============ Helper Methods ============

  private ResponseEntity<ErrorResponse> handleValidationError(BindingResult bindingResult) {
    Map<String, List<String>> details =
        bindingResult.getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

    return buildResponse(ErrorCode.VALIDATION_FAILED, null, details);
  }

  private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode) {
    return buildResponse(errorCode, null, null);
  }

  private ResponseEntity<ErrorResponse> buildResponse(
      ErrorCode errorCode, String customMessage, Map<String, List<String>> details) {

    String message = customMessage != null ? customMessage : errorCode.getMessage();
    ErrorResponse response = new ErrorResponse(errorCode.name(), message, details);

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }
}
