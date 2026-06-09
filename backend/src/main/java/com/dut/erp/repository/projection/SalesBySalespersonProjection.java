package com.dut.erp.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface SalesBySalespersonProjection {
  UUID getSalespersonId();
  String getSalespersonName();
  BigDecimal getTotalRevenue();
  Long getOrderCount();
}
