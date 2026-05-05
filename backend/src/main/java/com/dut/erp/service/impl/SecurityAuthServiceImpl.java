package com.dut.erp.service.impl;

import com.dut.erp.exception.AccessDeniedException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SecurityAuthService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("securityAuthService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityAuthServiceImpl implements SecurityAuthService {
  private final PermissionRepository permissionRepository;
  private final OrganizationRepository organizationRepository;

  @Override
  public void hasOrganizationAccess(UUID organizationId, CustomUserDetails userDetails) {
    boolean hasAccess =
        userDetails.getOrganizations().stream().anyMatch(org -> org.getId().equals(organizationId));

    if (!hasAccess) {
      if (!isOrganizationExist(organizationId)) {
        throw new ResourceNotFoundException(
            Map.of(
                "organizationId",
                List.of("Organization with ID " + organizationId + " not found.")));
      }
      throw new AccessDeniedException(
          Map.of(
              "organizationId",
              List.of("User does not have access to organization with ID " + organizationId)));
    }
  }

  @Override
  public void hasPermission(
      String permissionCode, UUID organizationId, CustomUserDetails userDetails) {
    if (!permissionRepository.existsByCodeAndUserIdAndOrganizationId(
        permissionCode, userDetails.getId(), organizationId))
      throw new AccessDeniedException(
          Map.of("permission", List.of("User does not have permission " + permissionCode)));
  }

  private boolean isOrganizationExist(UUID organizationId) {
    return organizationRepository.existsById(organizationId);
  }
}
