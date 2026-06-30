package com.dut.erp.service;

import com.dut.erp.entity.InventoryDocument;
import com.dut.erp.entity.Product;
import java.math.BigDecimal;
import java.util.UUID;

public interface COGSValuationEngine {
  void calculateCOGS(InventoryDocument document);
  BigDecimal estimateUnitCost(Product product, UUID warehouseId, BigDecimal quantity);
}
