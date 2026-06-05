package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpsertProductRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
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
  public PagedEntityResponse<ProductBaseResponse> getProductsWithFilterByOrganizationId(
      UUID organizationId, String search, PaginationRequest paginationRequest) {
    log.info("Fetching products for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? productRepository.findIdsByOrganizationIdAndSearch(organizationId, search, pageable)
            : productRepository.findIdsByOrganizationId(organizationId, pageable);

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
    Product product = findProductByIdAndOrganizationId(productId, organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse createProduct(UUID organizationId, UpsertProductRequest request) {
    Organization organization = findOrganizationById(organizationId);

    Product product =
        Product.builder()
            .organization(organization)
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
      UUID organizationId, UUID productId, UpsertProductRequest request) {
    Product product = findProductByIdAndOrganizationId(productId, organizationId);

    product.setName(request.name());
    product.setPrice(request.price());
    product.setDescription(request.description());

    product = productRepository.save(product);
    log.info("Updated product {} in organization {}", productId, organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public ProductResponse updateProductArchiveStatus(
      UUID organizationId, UUID productId, Boolean isArchived) {
    Product product = findProductByIdAndOrganizationId(productId, organizationId);
    product.setIsArchived(isArchived);
    product = productRepository.save(product);
    log.info("Updated archive status for product {} in organization {}", productId, organizationId);
    return productMapper.toResponse(product);
  }

  @Override
  @Transactional
  public void deleteProduct(UUID organizationId, UUID productId) {
    Product product = findProductByIdAndOrganizationId(productId, organizationId);
    productRepository.delete(product);
    log.info("Deleted product {} from organization {}", productId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private Product findProductByIdAndOrganizationId(UUID productId, UUID organizationId) {
    return productRepository
        .findByIdAndOrganizationId(productId, organizationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Product not found with id: " + productId));
  }
}
