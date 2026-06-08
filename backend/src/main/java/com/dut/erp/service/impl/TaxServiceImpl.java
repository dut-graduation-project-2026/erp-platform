package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpsertTaxRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.TaxBaseResponse;
import com.dut.erp.dto.response.TaxResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Tax;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.TaxMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.TaxRepository;
import com.dut.erp.service.TaxService;
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
public class TaxServiceImpl implements TaxService {

  private final OrganizationRepository organizationRepository;
  private final TaxRepository taxRepository;
  private final TaxMapper taxMapper;

  @Override
  public PagedEntityResponse<TaxBaseResponse> getTaxesWithFilterByOrganizationId(
      UUID organizationId, String search, boolean isArchived, PaginationRequest paginationRequest) {
    log.info("Fetching taxes for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? taxRepository.findIdsByOrganizationIdAndIsArchivedAndSearch(organizationId, isArchived, search, pageable)
            : taxRepository.findIdsByOrganizationIdAndIsArchived(organizationId, isArchived, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Tax> taxMap =
        taxRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Tax::getId, Function.identity()));

    List<TaxBaseResponse> responses =
        ids.getContent().stream()
            .map(taxMap::get)
            .filter(Objects::nonNull)
            .map(taxMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public TaxResponse getTaxById(UUID organizationId, UUID taxId) {
    log.info("Fetching tax {} for organization {}", taxId, organizationId);
    Tax tax = findTaxByIdAndOrganizationId(taxId, organizationId);
    return taxMapper.toResponse(tax);
  }

  @Override
  @Transactional
  public TaxResponse createTax(UUID organizationId, UpsertTaxRequest request) {
    Organization organization = findOrganizationById(organizationId);

    Tax tax =
        Tax.builder()
            .organization(organization)
            .name(request.name())
            .computation(request.computation())
            .amount(request.amount())
            .description(request.description())
            .build();

    tax = taxRepository.save(tax);
    log.info("Created tax {} in organization {}", tax.getId(), organizationId);
    return taxMapper.toResponse(tax);
  }

  @Override
  @Transactional
  public TaxResponse updateTax(
      UUID organizationId, UUID taxId, UpsertTaxRequest request) {
    Tax tax = findTaxByIdAndOrganizationId(taxId, organizationId);

    tax.setName(request.name());
    tax.setComputation(request.computation());
    tax.setAmount(request.amount());
    tax.setDescription(request.description());

    tax = taxRepository.save(tax);
    log.info("Updated tax {} in organization {}", taxId, organizationId);
    return taxMapper.toResponse(tax);
  }

  @Override
  @Transactional
  public TaxResponse updateTaxArchiveStatus(
      UUID organizationId, UUID taxId, boolean isArchived) {
    Tax tax = findTaxByIdAndOrganizationId(taxId, organizationId);
    tax.setArchived(isArchived);
    tax = taxRepository.save(tax);
    log.info("Updated archive status for tax {} in organization {}", taxId, organizationId);
    return taxMapper.toResponse(tax);
  }

  @Override
  @Transactional
  public void deleteTax(UUID organizationId, UUID taxId) {
    Tax tax = findTaxByIdAndOrganizationId(taxId, organizationId);
    taxRepository.delete(tax);
    log.info("Deleted tax {} from organization {}", taxId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private Tax findTaxByIdAndOrganizationId(UUID taxId, UUID organizationId) {
    return taxRepository
        .findByIdAndOrganizationId(taxId, organizationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Tax not found with id: " + taxId));
  }
}
