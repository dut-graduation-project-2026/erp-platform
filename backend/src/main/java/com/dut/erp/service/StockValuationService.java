package com.dut.erp.service;

import com.dut.erp.dto.response.StockValuationResponse;
import java.util.List;
import java.util.UUID;

public interface StockValuationService {
  List<StockValuationResponse> getValuationsByOrderId(UUID organizationId, UUID orderId);
}
