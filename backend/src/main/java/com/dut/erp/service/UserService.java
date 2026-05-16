package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import java.util.UUID;

public interface UserService {
  PagedEntityResponse<UserBaseResponse> getUsersByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  UserBaseResponse updateUser(UUID userId, UpdateUserRequest request);
}
