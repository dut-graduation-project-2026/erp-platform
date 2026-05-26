package com.dut.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SubmitInventoryCountRequest(
    @NotEmpty(message = "Count lines cannot be empty")
    @Valid
    List<SubmitCountLine> countLines
) {
  public record SubmitCountLine(
      @NotNull(message = "Line ID is required")
      UUID lineId,
      @NotNull(message = "Checked quantity is required")
      @DecimalMin(value = "0.00", message = "Checked quantity must be non-negative")
      BigDecimal checkedQty
  ) {}
}
