package com.dut.erp.dto.request;

import jakarta.validation.constraints.Min;

public record PaginationRequest(
    @Min(value = 1, message = "Page number must be greater than or equal to 1") Integer page,
    @Min(value = 1, message = "Limit must be at least 1") Integer limit) {
  public PaginationRequest {
    if (page == null) {
      page = 1;
    }

    if (limit == null) {
      limit = Integer.MAX_VALUE;
    }
  }
}
