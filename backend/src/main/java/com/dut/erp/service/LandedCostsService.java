package com.dut.erp.service;

import com.dut.erp.dto.request.LandedCostRequest;
import java.util.UUID;

public interface LandedCostsService {

  void allocateLandedCosts(UUID organizationId, UUID pickingId, LandedCostRequest request);
}
