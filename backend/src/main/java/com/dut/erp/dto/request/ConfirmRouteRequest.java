package com.dut.erp.dto.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ConfirmRouteRequest(
    @NotEmpty(message = "Route confirmations cannot be empty")
    List<RouteConfirmation> routeConfirmations
) {
  public record RouteConfirmation(
      @NotNull(message = "Order ID is required")
      UUID orderId,
      @NotNull(message = "Warehouse ID is required")
      UUID warehouseId
  ) {}
}
