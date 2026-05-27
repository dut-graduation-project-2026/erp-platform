package com.dut.erp.service;

import com.dut.erp.dto.response.SalesDashboardResponse;
import java.util.UUID;

public interface SalesReportService {

  SalesDashboardResponse getDashboard(UUID organizationId);
}
