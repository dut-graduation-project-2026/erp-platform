package com.dut.erp.service;

import com.dut.erp.dto.request.CreateCrmLeadRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.CrmLeadBaseResponse;
import com.dut.erp.dto.response.CrmLeadResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import java.util.UUID;

public interface CrmLeadService {

  PagedEntityResponse<CrmLeadBaseResponse> getLeadsByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  CrmLeadResponse getLeadById(UUID organizationId, UUID leadId);

  CrmLeadResponse createLead(UUID organizationId, CreateCrmLeadRequest request);

  CrmLeadResponse updateLead(UUID organizationId, UUID leadId, CreateCrmLeadRequest request);

  void deleteLead(UUID organizationId, UUID leadId);
}
