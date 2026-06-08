package com.dut.erp.service;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpsertProductRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import java.util.UUID;

public interface ProductService {

  PagedEntityResponse<ProductBaseResponse> getProductsWithFilterByOrganizationId(
      UUID organizationId, String search, boolean isArchived, PaginationRequest paginationRequest);

  ProductResponse getProductById(UUID organizationId, UUID productId);

  ProductResponse createProduct(UUID organizationId, UpsertProductRequest request);

  ProductResponse updateProduct(UUID organizationId, UUID productId, UpsertProductRequest request);

  ProductResponse updateProductArchiveStatus(
      UUID organizationId, UUID productId, boolean isArchived);

  void deleteProduct(UUID organizationId, UUID productId);
}
