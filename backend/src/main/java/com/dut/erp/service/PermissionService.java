package com.dut.erp.service;

import com.dut.erp.dto.response.PermissionResponse;
import java.util.List;
import java.util.UUID;

public interface PermissionService {
  List<PermissionResponse> getPermissionsByOrganizationIdAndUserId(UUID organizationId, UUID userId);
}
