package com.dut.erp.service;

import com.dut.erp.entity.Warehouse;
import java.math.BigDecimal;
import java.util.UUID;

public interface PickingOptimalService {
  Warehouse recommendOptimalWarehouse(UUID organizationId, UUID productId, BigDecimal quantity, UUID currentWarehouseId);
}
