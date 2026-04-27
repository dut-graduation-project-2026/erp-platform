package com.dut.erp.service.impl;

import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PermissionRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.SecurityService;
import com.dut.erp.util.AuthorityUtils;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("securityService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityServiceImpl implements SecurityService {
  private final UserRepository userRepository;
  private final PermissionRepository permissionRepository;
  private final OrganizationRepository organizationRepository;

  @Override
  public boolean isMemberOfOrganization(UUID userId, UUID organizationId) {
    if (userId == null || organizationId == null) {
      return false;
    }

    if (!organizationRepository.existsById(organizationId)) {
      return false;
    }

    return userRepository.findByIdAndOrganizationId(userId, organizationId).isPresent();
  }

  @Override
  public boolean hasAuthority(UUID userId, UUID organizationId, String authority) {
    String permission = AuthorityUtils.extractPermissionResource(authority);
    String action = AuthorityUtils.extractPermissionAction(authority);

    return permissionRepository.existsByUserIdAndOrganizationIdAndPermissionResourceAndPermissionAction(
        userId, organizationId, permission, action);
  }
}
