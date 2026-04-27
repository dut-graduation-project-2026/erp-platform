package com.dut.erp.service;

import com.dut.erp.dto.response.RoleResponse;
import java.util.List;
import java.util.UUID;

public interface RoleService {
  List<RoleResponse> getRolesByUserIdAndOrganizationId(UUID userId, UUID organizationId);
}
