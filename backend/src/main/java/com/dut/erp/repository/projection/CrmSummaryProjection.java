package com.dut.erp.repository.projection;

import java.math.BigDecimal;

public interface CrmSummaryProjection {
  Long getTotalLeads();
  Long getWonLeads();
  Long getLostLeads();
  BigDecimal getExpectedRevenue();
  BigDecimal getWonRevenue();
}
