package com.dut.erp.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface TopProductProjection {
  UUID getProductId();
  String getProductName();
  BigDecimal getTotalRevenue();
  BigDecimal getQuantitySold();
}
