package com.dut.erp.repository.projection;

import java.math.BigDecimal;

public interface RevenueTrendProjection {
  String getDateVal();
  BigDecimal getRevenue();
  Long getOrderCount();
}
