package com.dut.erp.exception;

import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class BaseException extends RuntimeException {

  private final ErrorCode errorCode;
  private final Map<String, List<String>> details;

  protected BaseException(ErrorCode errorCode) {
    this(errorCode, null, null);
  }

  protected BaseException(ErrorCode errorCode, String overrideMessage) {
    this(errorCode, overrideMessage, null);
  }

  protected BaseException(ErrorCode errorCode, Map<String, List<String>> details) {
    this(errorCode, null, details);
  }

  protected BaseException(
      ErrorCode errorCode, String overrideMessage, Map<String, List<String>> details) {

    super(overrideMessage != null ? overrideMessage : errorCode.getMessage());

    this.errorCode = errorCode;
    this.details = details;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public Map<String, List<String>> getDetails() {
    return details;
  }
}
