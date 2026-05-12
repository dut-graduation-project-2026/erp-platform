package com.dut.erp.service;

import com.dut.erp.security.CustomUserDetails;
import java.util.UUID;

public interface SecurityAuthService {
  boolean hasOrganizationAccess(UUID organizationId, CustomUserDetails userDetails);
  boolean hasPermission(String permissionCode, UUID organizationId, CustomUserDetails userDetails);
}
