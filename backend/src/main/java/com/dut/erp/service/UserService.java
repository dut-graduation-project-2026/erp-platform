package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.entity.User;
import java.util.UUID;

public interface UserService {
  PagedEntityResponse<UserBaseResponse> getUsersByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  PagedEntityResponse<UserBaseResponse> searchUsersByEmailContaining(
      String email, PaginationRequest paginationRequest);

  User findUserByIdFetchRolesAndOrganizations(UUID userId);
}
