package com.dut.erp.service;

import com.dut.erp.dto.response.PermissionResponse;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
  List<PermissionResponse> getAllPermissionsOfOrganizationWithActionsByOrganizationId(UUID organizationId);
  List<PermissionResponse> getAllPermissionsOfUserWithActionsByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
