package com.dut.erp.exception;

import com.dut.erp.enums.ErrorCode;
import java.util.List;
import java.util.Map;

public class ResourceAlreadyExistsException extends BaseException {
  public ResourceAlreadyExistsException() {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS);
  }

  public ResourceAlreadyExistsException(String message) {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS, message);
  }

  public ResourceAlreadyExistsException(Map<String, List<String>> details) {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS, details);
  }

  public ResourceAlreadyExistsException(String message, Map<String, List<String>> details) {
    super(ErrorCode.RESOURCE_ALREADY_EXISTS, message, details);
  }
}
