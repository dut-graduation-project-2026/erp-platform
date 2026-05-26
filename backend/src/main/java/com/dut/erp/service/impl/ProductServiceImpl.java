package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateProductRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateProductRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.ProductMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.service.ProductService;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

  private final OrganizationRepository organizationRepository;
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Override
  public PagedEntityResponse<ProductBaseResponse> getProductsByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching products for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids = productRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Product> productMap =
        productRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

    List<ProductBaseResponse> responses =
        ids.getContent().stream()
            .map(productMap::get)
            .filter(Objects::nonNull)
            .map(productMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public ProductResponse getProductById(UUID organizationId, UUID productId) {
    log.info("Fetching product {} for organization {}", productId, organizationId);
    Product product = findProductByIdAndVerifyOrganization(productId, organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse createProduct(UUID organizationId, CreateProductRequest request) {
    Organization organization = findOrganizationById(organizationId);
    assertSkuAvailable(request.sku(), organizationId);

    Product product =
        Product.builder()
            .organization(organization)
            .sku(request.sku())
            .name(request.name())
            .price(request.price())
            .description(request.description())
            .build();

    product = productRepository.save(product);
    log.info("Created product {} in organization {}", product.getId(), organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse updateProduct(
      UUID organizationId, UUID productId, UpdateProductRequest request) {
    findOrganizationById(organizationId);
    Product product = findProductByIdAndVerifyOrganization(productId, organizationId);

    product.setName(request.name());
    product.setPrice(request.price());
    product.setDescription(request.description());
    if (request.isActive() != null) {
      product.setIsActive(request.isActive());
    }

    product = productRepository.save(product);
    log.info("Updated product {} in organization {}", productId, organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public void deleteProduct(UUID organizationId, UUID productId) {
    findOrganizationById(organizationId);
    Product product = findProductByIdAndVerifyOrganization(productId, organizationId);
    productRepository.delete(product);
    log.info("Deleted product {} from organization {}", productId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Organization not found with id: " + organizationId));
  }

  private Product findProductByIdAndVerifyOrganization(UUID productId, UUID organizationId) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product not found with id: " + productId));
    if (!product.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Product does not belong to the specified organization.");
    }
    return product;
  }

  private void assertSkuAvailable(String sku, UUID organizationId) {
    if (productRepository.findBySkuAndOrganizationId(sku, organizationId).isPresent()) {
      throw new ResourceAlreadyExistsException(
          "Product with SKU '" + sku + "' already exists in this organization.");
    }
  }
}
