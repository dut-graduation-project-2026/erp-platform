package com.dut.erp.service;

import com.dut.erp.dto.request.UpdateOrganizationMemberRolesRequest;
import com.dut.erp.dto.response.OrganizationMemberResponse;
import java.util.UUID;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;

public interface OrganizationMemberService {
  PagedEntityResponse<OrganizationMemberResponse> getMembers(
      UUID organizationId, String query, PaginationRequest paginationRequest);

  OrganizationMemberResponse getMemberById(UUID organizationId, UUID userId);

  OrganizationMemberResponse updateMemberRoles(
      UUID organizationId, UUID userId, UpdateOrganizationMemberRolesRequest request);

  void removeMember(UUID organizationId, UUID userId);
}
