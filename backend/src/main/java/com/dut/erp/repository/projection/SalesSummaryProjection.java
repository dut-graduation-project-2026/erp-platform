package com.dut.erp.repository.projection;

import java.math.BigDecimal;

public interface SalesSummaryProjection {
  BigDecimal getTotalRevenue();
  Long getTotalOrders();
  BigDecimal getAverageOrderValue();
  Long getCompletedOrders();
  Long getCancelledOrders();
}
