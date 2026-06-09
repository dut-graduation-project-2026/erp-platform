package com.dut.erp.repository.projection;

import java.math.BigDecimal;

public interface CrmStageDistributionProjection {
  String getStage();
  Long getLeadCount();
  BigDecimal getExpectedRevenue();
}
