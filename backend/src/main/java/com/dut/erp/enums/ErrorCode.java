package com.dut.erp.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  // 400 Bad Request
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request is invalid or malformed."),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Some input fields are invalid. Please check your data."),

  // 401 Unauthorized
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Your session has expired or is invalid. Please log in again."),
  MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "Authentication is required. Please log in."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Session is invalid. Please log in again."),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Your session has expired. Please log in again."),

  // 403 Forbidden
  FORBIDDEN(HttpStatus.FORBIDDEN, "You do not have permission to perform this action."),
  ACCESS_DENIED(HttpStatus.FORBIDDEN, "You do not have permission to access this resource."),

  // 404 Not Found
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested item or page could not be found."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "No user account was found with the details provided."),

  // 409 Conflict
  CONFLICT(HttpStatus.CONFLICT, "This request conflicts with the current system state."),
  RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "This record already exists in the system."),

  // 500 Internal Server Error
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please try again later."),

  // 405 Method Not Allowed
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "This request method is not supported.");

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
