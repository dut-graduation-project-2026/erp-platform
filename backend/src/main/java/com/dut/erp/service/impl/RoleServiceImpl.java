package com.dut.erp.service.impl;

import com.dut.erp.dto.response.RoleResponse;
import com.dut.erp.mapper.RoleMapper;
import com.dut.erp.repository.RoleRepository;
import com.dut.erp.service.RoleService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
  private final RoleRepository roleRepository;
  private final RoleMapper roleMapper;

  @Override
  public List<RoleResponse> getRolesByUserIdAndOrganizationId(UUID userId, UUID organizationId) {
    return roleRepository.findRolesByUserIdAndOrganizationId(userId, organizationId).stream()
        .map(roleMapper::toRoleResponse)
        .toList();
  }
}
