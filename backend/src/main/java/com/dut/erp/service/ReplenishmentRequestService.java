package com.dut.erp.service;

import com.dut.erp.dto.request.CreateReplenishmentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ReplenishmentRequestResponse;
import java.util.UUID;

public interface ReplenishmentRequestService {

  ReplenishmentRequestResponse createReplenishmentRequest(
      UUID organizationId, UUID warehouseId, CreateReplenishmentRequest request);

  PagedEntityResponse<ReplenishmentRequestResponse> getReplenishmentRequests(
      UUID organizationId, UUID warehouseId, String search, String status, PaginationRequest paginationRequest);
}
