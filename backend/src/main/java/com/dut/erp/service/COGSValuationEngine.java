package com.dut.erp.service;

import com.dut.erp.entity.InventoryDocument;

public interface COGSValuationEngine {
  void calculateCOGS(InventoryDocument document);
}
