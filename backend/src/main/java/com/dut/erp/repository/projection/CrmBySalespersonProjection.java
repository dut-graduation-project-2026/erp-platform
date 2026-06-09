package com.dut.erp.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface CrmBySalespersonProjection {
  UUID getSalespersonId();
  String getSalespersonName();
  Long getTotalLeads();
  Long getWonLeads();
  BigDecimal getExpectedRevenue();
  BigDecimal getWonRevenue();
}
