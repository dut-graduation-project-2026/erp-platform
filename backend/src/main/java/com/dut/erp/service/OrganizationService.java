package com.dut.erp.service;

import com.dut.erp.dto.request.CreateOrganizationRequest;
import com.dut.erp.dto.request.UpdateOrganizationRequest;
import com.dut.erp.dto.response.OrganizationResponse;
import java.util.List;
import java.util.UUID;

public interface OrganizationService {
  OrganizationResponse createOrganization(UUID userId, CreateOrganizationRequest request);

  List<OrganizationResponse> getOrganizationsByUserId(UUID userId);

  OrganizationResponse getOrganizationById(UUID organizationId);

  OrganizationResponse updateOrganization(UUID organizationId, UpdateOrganizationRequest request);
}
