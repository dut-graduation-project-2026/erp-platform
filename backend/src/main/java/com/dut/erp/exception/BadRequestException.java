package com.dut.erp.exception;

import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class BadRequestException extends BaseException {
  public BadRequestException() {
    super(ErrorCode.BAD_REQUEST);
  }

  public BadRequestException(String message) {
    super(ErrorCode.BAD_REQUEST, message);
  }

  public BadRequestException(Map<String, List<String>> details) {
    super(ErrorCode.BAD_REQUEST, details);
  }

  public BadRequestException(String message, Map<String, List<String>> details) {
    super(ErrorCode.BAD_REQUEST, message, details);
  }
}
