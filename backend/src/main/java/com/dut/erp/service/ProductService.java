package com.dut.erp.service;

import com.dut.erp.dto.request.CreateProductRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateProductRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import java.util.UUID;

public interface ProductService {

  PagedEntityResponse<ProductBaseResponse> getProductsByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  ProductResponse getProductById(UUID organizationId, UUID productId);

  ProductResponse createProduct(UUID organizationId, CreateProductRequest request);

  ProductResponse updateProduct(UUID organizationId, UUID productId, UpdateProductRequest request);

  void deleteProduct(UUID organizationId, UUID productId);
}
