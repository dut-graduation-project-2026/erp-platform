package com.dut.erp.service;

import com.dut.erp.dto.request.CreateSalePartnerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateSalePartnerRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SalePartnerResponse;
import com.dut.erp.dto.response.SalePartnerBaseResponse;
import java.util.UUID;

public interface SalePartnerService {

  PagedEntityResponse<SalePartnerBaseResponse> getPartnersByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  SalePartnerResponse getPartnerById(UUID organizationId, UUID partnerId);

  SalePartnerResponse createPartner(UUID organizationId, CreateSalePartnerRequest request);

  SalePartnerResponse updatePartner(
      UUID organizationId, UUID partnerId, UpdateSalePartnerRequest request);

  void deletePartner(UUID organizationId, UUID partnerId);
}
