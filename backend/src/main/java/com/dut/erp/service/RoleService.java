package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.RoleBaseResponse;
import com.dut.erp.dto.response.RoleResponse;
import java.util.UUID;

public interface RoleService {
  PagedEntityResponse<RoleBaseResponse> getRolesByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  RoleResponse getRoleById(UUID roleId);
}
