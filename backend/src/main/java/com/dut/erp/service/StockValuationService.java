package com.dut.erp.service;

import com.dut.erp.entity.StockMove;
import com.dut.erp.enums.CostMethod;
import java.math.BigDecimal;
import java.util.UUID;

public interface StockValuationService {

  BigDecimal calculateCOGS(StockMove move, CostMethod method);

  BigDecimal getCogsForSaleOrder(UUID organizationId, UUID saleOrderId);
}
