package com.dut.erp.service;

import com.dut.erp.dto.request.CreateStockLotRequest;
import com.dut.erp.dto.response.StockLotResponse;
import java.util.List;
import java.util.UUID;

public interface StockLotService {

  StockLotResponse createLot(UUID organizationId, CreateStockLotRequest request);

  StockLotResponse getLotById(UUID organizationId, UUID lotId);

  List<StockLotResponse> getLotsByProductId(UUID organizationId, UUID productId);
}
