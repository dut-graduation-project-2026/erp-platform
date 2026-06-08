package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpsertTaxRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.TaxBaseResponse;
import com.dut.erp.dto.response.TaxResponse;
import java.util.UUID;

public interface TaxService {

  PagedEntityResponse<TaxBaseResponse> getTaxesWithFilterByOrganizationId(
      UUID organizationId, String search, boolean isArchived, PaginationRequest paginationRequest);

  TaxResponse getTaxById(UUID organizationId, UUID taxId);

  TaxResponse createTax(UUID organizationId, UpsertTaxRequest request);

  TaxResponse updateTax(UUID organizationId, UUID taxId, UpsertTaxRequest request);

  TaxResponse updateTaxArchiveStatus(UUID organizationId, UUID taxId, boolean isArchived);

  void deleteTax(UUID organizationId, UUID taxId);
}
