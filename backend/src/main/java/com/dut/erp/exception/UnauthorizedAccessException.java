package com.dut.erp.exception;

import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class UnauthorizedAccessException extends BaseException {
  public UnauthorizedAccessException() {
    super(ErrorCode.UNAUTHORIZED);
  }

  public UnauthorizedAccessException(String message) {
    super(ErrorCode.UNAUTHORIZED, message);
  }

  public UnauthorizedAccessException(Map<String, List<String>> details) {
    super(ErrorCode.UNAUTHORIZED, details);
  }

  public UnauthorizedAccessException(String message, Map<String, List<String>> details) {
    super(ErrorCode.UNAUTHORIZED, message, details);
  }
}
