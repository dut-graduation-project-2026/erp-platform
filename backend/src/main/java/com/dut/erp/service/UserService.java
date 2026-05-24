package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.dto.response.UserPermissionsResponse;
import java.util.UUID;
import java.util.List;

public interface UserService {
  PagedEntityResponse<UserBaseResponse> searchUsersByOrganizationId(
      UUID organizationId, String query, PaginationRequest paginationRequest);

  UserBaseResponse updateUser(UUID userId, UpdateUserRequest request);

  UserPermissionsResponse getMyPermissions(UUID userId, UUID organizationId);
}
