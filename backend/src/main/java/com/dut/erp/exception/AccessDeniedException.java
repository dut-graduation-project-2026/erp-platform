package com.dut.erp.exception;

import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class AccessDeniedException extends BaseException {
  public AccessDeniedException() {
    super(ErrorCode.ACCESS_DENIED);
  }

  public AccessDeniedException(String message) {
    super(ErrorCode.ACCESS_DENIED, message);
  }

  public AccessDeniedException(Map<String, List<String>> details) {
    super(ErrorCode.ACCESS_DENIED, details);
  }

  public AccessDeniedException(String message, Map<String, List<String>> details) {
    super(ErrorCode.ACCESS_DENIED, message, details);
  }
}
