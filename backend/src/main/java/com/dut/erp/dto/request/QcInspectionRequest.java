package com.dut.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record QcInspectionRequest(
    @NotEmpty(message = "QC lines cannot be empty") @Valid List<QcLine> qcLines) {
  public record QcLine(
      @NotNull(message = "Move ID is required") UUID moveId,
      @NotNull(message = "Approved quantity is required")
          @DecimalMin(value = "0.00", message = "Approved quantity must be non-negative")
          BigDecimal approvedQty) {}
}
