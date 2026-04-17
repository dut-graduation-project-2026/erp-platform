package com.dut.erp.service;

import com.dut.erp.dto.response.OrganizationResponse;
import java.util.List;
import java.util.UUID;

public interface OrganizationService {
  List<OrganizationResponse> getOrganizationByUserId(UUID userId);
}
