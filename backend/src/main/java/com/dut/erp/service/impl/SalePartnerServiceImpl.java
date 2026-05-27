package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateSalePartnerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.PartnerContactRequest;
import com.dut.erp.dto.request.UpdateSalePartnerRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SalePartnerBaseResponse;
import com.dut.erp.dto.response.SalePartnerResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.PartnerContact;
import com.dut.erp.entity.SalePartner;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceAlreadyExistsException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.SalePartnerMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.SalePartnerRepository;
import com.dut.erp.service.SalePartnerService;
import java.util.ArrayList;
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
public class SalePartnerServiceImpl implements SalePartnerService {

  private final OrganizationRepository organizationRepository;
  private final SalePartnerRepository salePartnerRepository;
  private final SalePartnerMapper salePartnerMapper;

  @Override
  public PagedEntityResponse<SalePartnerBaseResponse> getPartnersByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching sale partners for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    Page<UUID> ids = salePartnerRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, SalePartner> partnerMap =
        salePartnerRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(SalePartner::getId, Function.identity()));

    List<SalePartnerBaseResponse> responses =
        ids.getContent().stream()
            .map(partnerMap::get)
            .filter(Objects::nonNull)
            .map(salePartnerMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public SalePartnerResponse getPartnerById(UUID organizationId, UUID partnerId) {
    log.info("Fetching sale partner {} for organization {}", partnerId, organizationId);
    SalePartner partner = findPartnerByIdAndVerifyOrganization(partnerId, organizationId);
    return salePartnerMapper.toResponse(partner);
  }

  @Override
  @Transactional
  public SalePartnerResponse createPartner(UUID organizationId, CreateSalePartnerRequest request) {
    Organization organization = findOrganizationById(organizationId);
    assertCodeAvailable(request.code(), organizationId);

    SalePartner partner =
        SalePartner.builder()
            .organization(organization)
            .code(request.code())
            .name(request.name())
            .partnerType(request.partnerType())
            .taxCode(request.taxCode())
            .email(request.email())
            .phone(request.phone())
            .address(request.address())
            .build();

    if (request.contacts() != null) {
      List<PartnerContact> contacts =
          request.contacts().stream()
              .map(
                  c ->
                      PartnerContact.builder()
                          .partner(partner)
                          .name(c.name())
                          .email(c.email())
                          .phone(c.phone())
                          .position(c.position())
                          .isPrimary(c.isPrimary() != null ? c.isPrimary() : Boolean.FALSE)
                          .build())
              .collect(Collectors.toList());
      partner.setContacts(contacts);
    }

    SalePartner savedPartner = salePartnerRepository.save(partner);
    log.info("Created sale partner {} in organization {}", savedPartner.getId(), organizationId);
    return salePartnerMapper.toResponse(savedPartner);
  }

  @Override
  @Transactional
  public SalePartnerResponse updatePartner(
      UUID organizationId, UUID partnerId, UpdateSalePartnerRequest request) {
    findOrganizationById(organizationId);
    SalePartner partner = findPartnerByIdAndVerifyOrganization(partnerId, organizationId);

    partner.setName(request.name());
    partner.setPartnerType(request.partnerType());
    partner.setTaxCode(request.taxCode());
    partner.setEmail(request.email());
    partner.setPhone(request.phone());
    partner.setAddress(request.address());
    if (request.status() != null) {
      partner.setStatus(request.status());
    }

    // Synchronize contacts
    if (request.contacts() != null) {
      Map<UUID, PartnerContact> existingContacts =
          partner.getContacts().stream()
              .filter(c -> c.getId() != null)
              .collect(Collectors.toMap(PartnerContact::getId, Function.identity()));

      List<PartnerContact> updatedContacts = new ArrayList<>();
      for (PartnerContactRequest req : request.contacts()) {
        if (req.id() != null && existingContacts.containsKey(req.id())) {
          PartnerContact contact = existingContacts.get(req.id());
          contact.setName(req.name());
          contact.setEmail(req.email());
          contact.setPhone(req.phone());
          contact.setPosition(req.position());
          contact.setIsPrimary(req.isPrimary() != null ? req.isPrimary() : Boolean.FALSE);
          updatedContacts.add(contact);
        } else {
          updatedContacts.add(
              PartnerContact.builder()
                  .partner(partner)
                  .name(req.name())
                  .email(req.email())
                  .phone(req.phone())
                  .position(req.position())
                  .isPrimary(req.isPrimary() != null ? req.isPrimary() : Boolean.FALSE)
                  .build());
        }
      }

      partner.getContacts().clear();
      partner.getContacts().addAll(updatedContacts);
    } else {
      partner.getContacts().clear();
    }

    partner = salePartnerRepository.save(partner);
    log.info("Updated sale partner {} in organization {}", partnerId, organizationId);
    return salePartnerMapper.toResponse(partner);
  }

  @Override
  @Transactional
  public void deletePartner(UUID organizationId, UUID partnerId) {
    findOrganizationById(organizationId);
    SalePartner partner = findPartnerByIdAndVerifyOrganization(partnerId, organizationId);
    salePartnerRepository.delete(partner);
    log.info("Deleted sale partner {} from organization {}", partnerId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private SalePartner findPartnerByIdAndVerifyOrganization(UUID partnerId, UUID organizationId) {
    SalePartner partner =
        salePartnerRepository
            .findByIdWithContacts(partnerId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Sale partner not found with id: " + partnerId));
    if (!partner.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Sale partner does not belong to the specified organization.");
    }
    return partner;
  }

  private void assertCodeAvailable(String code, UUID organizationId) {
    if (salePartnerRepository.findByCodeAndOrganizationId(code, organizationId).isPresent()) {
      throw new ResourceAlreadyExistsException(
          "Sale partner with code '" + code + "' already exists in this organization.");
    }
  }
}
