package com.dut.erp.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface TopCustomerProjection {
  UUID getPartnerId();
  String getPartnerName();
  BigDecimal getTotalSpend();
  Long getOrderCount();
}
