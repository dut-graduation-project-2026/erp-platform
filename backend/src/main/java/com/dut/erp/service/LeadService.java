package com.dut.erp.service;

import com.dut.erp.dto.request.CreateLeadRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateLeadRequest;
import com.dut.erp.dto.response.LeadBaseResponse;
import com.dut.erp.dto.response.LeadResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import java.util.UUID;

public interface LeadService {

  PagedEntityResponse<LeadBaseResponse> getLeadsWithFilterByOrganizationId(
      UUID organizationId, String search, PaginationRequest paginationRequest);

  LeadResponse getLeadById(UUID organizationId, UUID leadId);

  LeadResponse createLead(
      UUID organizationId, CreateLeadRequest request, CustomUserDetails userDetails);

  LeadResponse updateLead(UUID organizationId, UUID leadId, UpdateLeadRequest request);

  LeadResponse updateLeadStage(UUID organizationId, UUID leadId, String stage);

  void deleteLead(UUID organizationId, UUID leadId);
}
