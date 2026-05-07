package com.dut.erp.service.impl;

import com.dut.erp.dto.response.ErpModuleBaseResponse;
import com.dut.erp.dto.response.ErpModuleResponse;
import com.dut.erp.entity.ErpModule;
import com.dut.erp.mapper.ErpModuleMapper;
import com.dut.erp.repository.ErpModuleRepository;
import com.dut.erp.service.ErpModuleService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErpModuleServiceImpl implements ErpModuleService {
  private final ErpModuleRepository erpModuleRepository;
  private final ErpModuleMapper erpModuleMapper;

  @Override
  public List<ErpModuleBaseResponse> getErpModulesByUserAndOrganization(
      UUID userId, UUID organizationId) {
    List<ErpModule> modules =
        erpModuleRepository.findAllAccessibleByUserIdAndOrganizationId(userId, organizationId);
    return modules.stream()
        .map(erpModuleMapper::toErpModuleBaseResponse)
        .collect(Collectors.toList());
  }

  @Override
  public List<ErpModuleResponse> getErpModulesByOrganization(UUID organizationId) {
    List<ErpModule> modules =
        erpModuleRepository.findAllByOrganizationIdWithPermissions(organizationId);
    return modules.stream().map(erpModuleMapper::toErpModuleResponse).collect(Collectors.toList());
  }
}
