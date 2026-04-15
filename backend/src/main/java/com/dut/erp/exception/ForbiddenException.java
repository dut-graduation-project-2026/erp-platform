package com.dut.erp.exception;

import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class ForbiddenException extends BaseException {
  public ForbiddenException() {
    super(ErrorCode.FORBIDDEN);
  }

  public ForbiddenException(String message) {
    super(ErrorCode.FORBIDDEN, message);
  }

  public ForbiddenException(Map<String, List<String>> details) {
    super(ErrorCode.FORBIDDEN, details);
  }

  public ForbiddenException(String message, Map<String, List<String>> details) {
    super(ErrorCode.FORBIDDEN, message, details);
  }
}
