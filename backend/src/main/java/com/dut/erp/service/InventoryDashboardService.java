package com.dut.erp.service;

import com.dut.erp.dto.response.InventoryDashboardResponse;
import java.util.UUID;

public interface InventoryDashboardService {
  InventoryDashboardResponse getDashboard(UUID organizationId);
}
