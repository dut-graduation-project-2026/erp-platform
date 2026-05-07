package com.dut.erp.service.impl;

import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SecurityAuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("securityAuthService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityAuthServiceImpl implements SecurityAuthService {

  private final PermissionRepository permissionRepository;

  @Override
  public boolean hasOrganizationAccess(UUID organizationId, CustomUserDetails userDetails) {
    boolean hasAccess =
        userDetails.getOrganizations().stream().anyMatch(org -> org.getId().equals(organizationId));
    if (!hasAccess) {
      log.warn("User {} denied access to organization {}", userDetails.getId(), organizationId);
      throw new AccessDeniedException("Access denied");
    }

    return true;
  }

  @Override
  public boolean hasPermission(
      String permissionCode, UUID organizationId, CustomUserDetails userDetails) {
    boolean hasPermission =
        permissionRepository.existsByCodeAndUserIdAndOrganizationId(
            permissionCode, userDetails.getId(), organizationId);

    if (!hasPermission) {
      log.warn(
          "User {} missing permission '{}' for organization {}",
          userDetails.getId(),
          permissionCode,
          organizationId);
      throw new AccessDeniedException("Access denied");
    }

    return true;
  }
}
