package com.dut.erp.service;

import com.dut.erp.dto.request.CreateProductTemplateRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateProductTemplateRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductTemplateBaseResponse;
import com.dut.erp.dto.response.ProductTemplateResponse;
import java.util.UUID;

public interface ProductTemplateService {

  PagedEntityResponse<ProductTemplateBaseResponse> getTemplatesByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  ProductTemplateResponse getTemplateById(UUID organizationId, UUID templateId);

  ProductTemplateResponse createTemplate(UUID organizationId, CreateProductTemplateRequest request);

  ProductTemplateResponse updateTemplate(
      UUID organizationId, UUID templateId, UpdateProductTemplateRequest request);

  void deleteTemplate(UUID organizationId, UUID templateId);
}
