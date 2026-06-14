package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.ProductCategoryRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductCategoryBaseResponse;
import com.dut.erp.dto.response.ProductCategoryResponse;
import java.util.UUID;

public interface ProductCategoryService {
  PagedEntityResponse<ProductCategoryBaseResponse> getCategoriesByOrganizationId(
      UUID organizationId, String search, PaginationRequest paginationRequest);

  ProductCategoryResponse getCategoryById(UUID organizationId, UUID categoryId);

  ProductCategoryResponse createCategory(UUID organizationId, ProductCategoryRequest request);

  ProductCategoryResponse updateCategory(UUID organizationId, UUID categoryId, ProductCategoryRequest request);

  void deleteCategory(UUID organizationId, UUID categoryId);
}
