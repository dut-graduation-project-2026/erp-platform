package com.dut.erp.service;

import com.dut.erp.security.CustomUserDetails;
import java.util.UUID;

public interface SecurityAuthService {
  void hasOrganizationAccess(UUID organizationId, CustomUserDetails userDetails);
  void hasPermission(String permission, UUID organizationId, CustomUserDetails userDetails);
}
