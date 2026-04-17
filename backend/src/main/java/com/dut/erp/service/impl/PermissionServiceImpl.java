package com.dut.erp.service.impl;

import com.dut.erp.dto.response.PermissionResponse;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.PermissionMapper;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.service.PermissionService;
import com.dut.erp.service.UserService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PermissionServiceImpl implements PermissionService {
  private final UserService userService;

  private final PermissionRepository permissionRepository;
  private final PermissionMapper permissionMapper;

  @Override
  public List<PermissionResponse> getPermissionsByOrganizationIdAndUserId(UUID organizationId, UUID userId) {
    if (!userService.existsById(userId)) {
      throw new ResourceNotFoundException("User with ID " + userId + " does not exist");
    }
    return permissionRepository.findAllByOrganizationIdAndUserIdWithActions(organizationId, userId).stream()
        .map(permissionMapper::toPermissionResponse)
        .toList();
  }
}
