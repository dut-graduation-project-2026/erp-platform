package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.ProductCategoryRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductCategoryBaseResponse;
import com.dut.erp.dto.response.ProductCategoryResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.ProductCategory;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.ProductCategoryMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductCategoryRepository;
import com.dut.erp.service.ProductCategoryService;
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
public class ProductCategoryServiceImpl implements ProductCategoryService {

  private final OrganizationRepository organizationRepository;
  private final ProductCategoryRepository productCategoryRepository;
  private final ProductCategoryMapper productCategoryMapper;

  @Override
  public PagedEntityResponse<ProductCategoryBaseResponse> getCategoriesByOrganizationId(
      UUID organizationId, String search, PaginationRequest paginationRequest) {
    log.info("Fetching product categories for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? productCategoryRepository.findIdsByOrganizationIdAndSearch(organizationId, search, pageable)
            : productCategoryRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, ProductCategory> categoryMap =
        productCategoryRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(ProductCategory::getId, Function.identity()));

    List<ProductCategoryBaseResponse> responses =
        ids.getContent().stream()
            .map(categoryMap::get)
            .filter(Objects::nonNull)
            .map(productCategoryMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public ProductCategoryResponse getCategoryById(UUID organizationId, UUID categoryId) {
    log.info("Fetching product category {} for organization {}", categoryId, organizationId);
    ProductCategory category = findCategoryByIdAndOrganizationId(categoryId, organizationId);
    return productCategoryMapper.toResponse(category);
  }

  @Override
  @Transactional
  public ProductCategoryResponse createCategory(UUID organizationId, ProductCategoryRequest request) {
    Organization organization = findOrganizationById(organizationId);

    if (productCategoryRepository.existsByOrganizationIdAndName(organizationId, request.name())) {
      log.warn("Category creation failed: name {} already exists in organization {}", request.name(), organizationId);
      throw new ResourceAlreadyExistsException("Category with this name already exists in the organization.");
    }

    ProductCategory category =
        ProductCategory.builder()
            .organization(organization)
            .name(request.name())
            .description(request.description())
            .build();

    category = productCategoryRepository.save(category);
    log.info("Created product category {} in organization {}", category.getId(), organizationId);

    return productCategoryMapper.toResponse(category);
  }

  @Override
  @Transactional
  public ProductCategoryResponse updateCategory(
      UUID organizationId, UUID categoryId, ProductCategoryRequest request) {
    ProductCategory category = findCategoryByIdAndOrganizationId(categoryId, organizationId);

    if (productCategoryRepository.existsByOrganizationIdAndNameAndIdNot(organizationId, request.name(), categoryId)) {
      log.warn("Category update failed: name {} already exists in organization {}", request.name(), organizationId);
      throw new ResourceAlreadyExistsException("Category with this name already exists in the organization.");
    }

    category.setName(request.name());
    category.setDescription(request.description());

    category = productCategoryRepository.save(category);
    log.info("Updated product category {} in organization {}", categoryId, organizationId);

    return productCategoryMapper.toResponse(category);
  }

  @Override
  @Transactional
  public void deleteCategory(UUID organizationId, UUID categoryId) {
    ProductCategory category = findCategoryByIdAndOrganizationId(categoryId, organizationId);

    if (productCategoryRepository.hasProductsLinked(categoryId)) {
      log.warn("Cannot delete product category {} because products are linked to it", categoryId);
      throw new BadRequestException("Cannot delete product category because active products are linked to it.");
    }

    productCategoryRepository.delete(category);
    log.info("Deleted product category {} from organization {}", categoryId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private ProductCategory findCategoryByIdAndOrganizationId(UUID categoryId, UUID organizationId) {
    return productCategoryRepository
        .findByIdAndOrganizationId(categoryId, organizationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Product category not found with id: " + categoryId));
  }
}
