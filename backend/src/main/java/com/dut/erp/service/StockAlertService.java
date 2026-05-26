package com.dut.erp.service;

import com.dut.erp.dto.response.StockAlertResponse;
import java.util.List;
import java.util.UUID;

public interface StockAlertService {
  List<StockAlertResponse> getAlerts(UUID organizationId);
}
