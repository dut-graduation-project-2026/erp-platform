package com.dut.erp.service.impl;

import com.dut.erp.dto.response.PermissionResponse;
import com.dut.erp.mapper.PermissionMapper;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.service.PermissionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {
  private final PermissionRepository permissionRepository;
  private final PermissionMapper permissionMapper;

  @Override
  public List<PermissionResponse> getAllPermissionsOfOrganizationWithActionsByOrganizationId(
      UUID organizationId) {
    return permissionRepository.findAllByOrganizationIdWithActions(organizationId).stream()
        .map(permissionMapper::toPermissionResponse)
        .toList();
  }

    // @Override
    // public List<PermissionResponse> getAllPermissionsOfUserWithActionsByUserRoleIdAndOrganizationId(
    //     UUID userRoleId, UUID organizationId) {
    //   return permissionRepository.findAllByUserRoleIdAndOrganizationIdWithActions(userRoleId, organizationId).stream()
    //       .map(permissionMapper::toPermissionResponse)
    //       .toList();
    // }

    @Override
    public List<PermissionResponse> getAllPermissionsOfUserWithActionsByUserIdAndOrganizationId(UUID userId,
            UUID organizationId) {
        // TODO Auto-generated method stub
        return null;
    }
}
