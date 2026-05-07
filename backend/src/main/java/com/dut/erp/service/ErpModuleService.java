package com.dut.erp.service;

import com.dut.erp.dto.response.ErpModuleBaseResponse;
import com.dut.erp.dto.response.ErpModuleResponse;
import java.util.List;
import java.util.UUID;

public interface ErpModuleService {
  List<ErpModuleBaseResponse> getErpModulesByUserAndOrganization(UUID userId, UUID organizationId);

  List<ErpModuleResponse> getErpModulesByOrganization(UUID organizationId);
}
