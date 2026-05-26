package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateProductTemplateRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateProductTemplateRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductTemplateBaseResponse;
import com.dut.erp.dto.response.ProductTemplateResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Product;
import com.dut.erp.entity.ProductTemplate;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.ProductTemplateMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.ProductRepository;
import com.dut.erp.repository.ProductTemplateRepository;
import com.dut.erp.service.ProductTemplateService;
import java.math.BigDecimal;
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
public class ProductTemplateServiceImpl implements ProductTemplateService {

  private final OrganizationRepository organizationRepository;
  private final ProductTemplateRepository productTemplateRepository;
  private final ProductRepository productRepository;
  private final ProductTemplateMapper productTemplateMapper;

  @Override
  public PagedEntityResponse<ProductTemplateBaseResponse> getTemplatesByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching product templates for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(
                SortField.asc("name")));

    Page<UUID> ids = productTemplateRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, ProductTemplate> templateMap =
        productTemplateRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(ProductTemplate::getId, Function.identity()));

    List<ProductTemplateBaseResponse> responses =
        ids.getContent().stream()
            .map(templateMap::get)
            .filter(Objects::nonNull)
            .map(productTemplateMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public ProductTemplateResponse getTemplateById(UUID organizationId, UUID templateId) {
    log.info("Fetching template {} for organization {}", templateId, organizationId);
    ProductTemplate template = findTemplateByIdAndVerifyOrganization(templateId, organizationId);
    return productTemplateMapper.toResponse(template);
  }

  @Override
  @Transactional
  public ProductTemplateResponse createTemplate(UUID organizationId, CreateProductTemplateRequest request) {
    log.info("Creating product template {} in organization {}", request.name(), organizationId);
    Organization organization = findOrganizationById(organizationId);

    ProductTemplate template =
        ProductTemplate.builder()
            .organization(organization)
            .name(request.name())
            .description(request.description())
            .category(request.category())
            .uom(request.uom())
            .valuationMethod(request.valuationMethod())
            .isActive(true)
            .build();

    template = productTemplateRepository.save(template);

    // Auto-create default variant
    String defaultSku = "SKU-" + template.getId().toString().substring(0, 8).toUpperCase();
    Product defaultVariant =
        Product.builder()
            .organization(organization)
            .productTemplate(template)
            .sku(defaultSku)
            .name(template.getName())
            .price(BigDecimal.ZERO)
            .cost(BigDecimal.ZERO)
            .isActive(true)
            .build();
    productRepository.save(defaultVariant);
    log.info("Created default variant {} for template {}", defaultSku, template.getId());

    return productTemplateMapper.toResponse(template);
  }

  @Override
  @Transactional
  public ProductTemplateResponse updateTemplate(
      UUID organizationId, UUID templateId, UpdateProductTemplateRequest request) {
    log.info("Updating template {} in organization {}", templateId, organizationId);
    findOrganizationById(organizationId);
    ProductTemplate template = findTemplateByIdAndVerifyOrganization(templateId, organizationId);

    template.setName(request.name());
    template.setDescription(request.description());
    template.setCategory(request.category());
    template.setUom(request.uom());
    template.setValuationMethod(request.valuationMethod());
    if (request.isActive() != null) {
      template.setIsActive(request.isActive());
    }

    template = productTemplateRepository.save(template);
    return productTemplateMapper.toResponse(template);
  }

  @Override
  @Transactional
  public void deleteTemplate(UUID organizationId, UUID templateId) {
    log.info("Deleting template {} from organization {}", templateId, organizationId);
    findOrganizationById(organizationId);
    ProductTemplate template = findTemplateByIdAndVerifyOrganization(templateId, organizationId);
    productTemplateRepository.delete(template);
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

  private ProductTemplate findTemplateByIdAndVerifyOrganization(UUID templateId, UUID organizationId) {
    ProductTemplate template =
        productTemplateRepository
            .findById(templateId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Product template not found with id: " + templateId));
    if (!template.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Product template does not belong to the specified organization.");
    }
    return template;
  }
}
