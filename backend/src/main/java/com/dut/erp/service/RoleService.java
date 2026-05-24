package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.CreateRoleRequest;
import com.dut.erp.dto.request.UpdateRoleRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.dto.response.RoleResponse;
import java.util.UUID;

public interface RoleService {
  PagedEntityResponse<RoleBaseResponse> getRolesByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  RoleResponse createRole(UUID organizationId, CreateRoleRequest request);

  RoleResponse getRoleByIdWithOrganizationAndPermissionAndModule(UUID roleId);

  RoleResponse updateRole(UUID roleId, UUID organizationId, UpdateRoleRequest request);

  void deleteRole(UUID roleId, UUID organizationId);

  boolean isRoleBelongsToOrganization(UUID roleId, UUID organizationId);
}
