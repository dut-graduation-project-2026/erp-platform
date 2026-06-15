package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.dto.response.OrganizationMemberResponse;
import java.util.List;
import java.util.UUID;


public interface UserService {
  PagedEntityResponse<OrganizationMemberResponse> searchUsersByOrganizationId(
      UUID organizationId, String query, PaginationRequest paginationRequest);

  UserBaseResponse updateUser(UUID userId, UpdateUserRequest request);

  OrganizationMemberResponse getUserByIdAndOrganizationId(UUID userId, UUID organizationId);

  OrganizationMemberResponse updateUserRoles(UUID userId, UUID organizationId, List<UUID> roleIds);

  void removeUserFromOrganization(UUID userId, UUID organizationId);

  void changePassword(UUID userId, com.dut.erp.dto.request.ChangePasswordRequest request);
}
