package com.dut.erp.service;

import com.dut.erp.dto.request.CreateRoleRequest;
import com.dut.erp.dto.response.RoleResponse;
import java.util.List;
import java.util.UUID;

public interface RoleService {
  RoleResponse addRole(CreateRoleRequest requestd);
  List<RoleResponse> getRolesByUserIdAndOrganizationId(UUID userId, UUID organizationId);
  List<RoleResponse> getAllRolesByOrganizationId(UUID organizationId);
  RoleResponse getRoleById(UUID roleId);
}
