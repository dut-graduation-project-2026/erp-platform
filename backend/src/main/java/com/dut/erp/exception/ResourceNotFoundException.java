package com.dut.erp.exception;

import java.util.List;
import java.util.Map;

import com.dut.erp.enums.ErrorCode;

public class ResourceNotFoundException extends BaseException{
    public ResourceNotFoundException() {
        super(ErrorCode.RESOURCE_NOT_FOUND);
    }

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public ResourceNotFoundException(Map<String, List<String>> details) {
        super(ErrorCode.RESOURCE_NOT_FOUND, details);
    }

    public ResourceNotFoundException(String message, Map<String, List<String>> details) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message, details);
    }
}
