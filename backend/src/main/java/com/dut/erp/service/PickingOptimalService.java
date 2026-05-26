package com.dut.erp.service;

import com.dut.erp.dto.response.StockQuantResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PickingOptimalService {

  List<StockQuantResponse> suggestLocations(UUID organizationId, UUID productId, BigDecimal quantity);
}
