package com.dut.erp.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  // 400 Bad Request
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request."),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed."),

  // 401 Unauthorized
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized."),
  MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "Authentication token is missing."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid authentication token."),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Authentication token expired."),

  // 403 Forbidden
  FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied."),

  // 404 Not Found
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found."),

  // 409 Conflict
  CONFLICT(HttpStatus.CONFLICT, "Conflict occurred."),
  RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Resource already exists."),

  // 500 Internal Server Error
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error."),

  // 405 Method Not Allowed
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed.");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
