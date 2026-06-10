package com.dut.erp.service;

import com.dut.erp.entity.Order;
import java.util.UUID;

public interface SalesOrderIntegrationService {
  void handleOrderConfirmation(Order order, UUID warehouseId);
}
