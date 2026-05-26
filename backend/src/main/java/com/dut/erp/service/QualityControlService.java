package com.dut.erp.service;

import com.dut.erp.dto.request.QcInspectionRequest;
import com.dut.erp.dto.response.StockPickingResponse;
import java.util.UUID;

public interface QualityControlService {

  StockPickingResponse inspectPicking(UUID organizationId, UUID pickingId, QcInspectionRequest request);
}
