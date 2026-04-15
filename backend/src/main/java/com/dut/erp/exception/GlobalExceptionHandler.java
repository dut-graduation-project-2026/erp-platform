package com.dut.erp.exception;

import com.dut.erp.dto.response.ErrorResponse;
import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BaseException ex) {
    return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), ex.getDetails());
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    return buildErrorResponse(ErrorCode.UNAUTHORIZED, null, null);
  }

  @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(Exception ex) {
    return buildErrorResponse(ErrorCode.RESOURCE_NOT_FOUND, null, null);
  }

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ErrorResponse> handleValidationException(BindException ex) {
    Map<String, List<String>> details =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

    ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
    return buildErrorResponse(errorCode, null, details);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
    log.error("Unexpected error: {}", ex);
    return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      ErrorCode errorCode, String customMessage, Map<String, List<String>> details) {

    String message = customMessage != null ? customMessage : errorCode.getMessage();

    ErrorResponse response = new ErrorResponse(errorCode.name(), message, details);

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }
}
