package com.dut.erp.service.impl;

import com.dut.erp.dto.request.CreatePartnerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.PartnerContactRequest;
import com.dut.erp.dto.request.UpdateArchiveStatusRequest;
import com.dut.erp.dto.request.UpdatePartnerRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.PartnerBaseResponse;
import com.dut.erp.dto.response.PartnerResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Partner;
import com.dut.erp.entity.PartnerContact;
import com.dut.erp.enums.PartnerType;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.PartnerMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PartnerRepository;
import com.dut.erp.service.PartnerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
public class PartnerServiceImpl implements PartnerService {

  private final PartnerRepository partnerRepository;
  private final OrganizationRepository organizationRepository;
  private final PartnerMapper partnerMapper;

  @Override
  @Transactional
  public PartnerResponse createPartner(UUID organizationId, CreatePartnerRequest request) {
    Organization organization = findOrganizationById(organizationId);

    Partner partner =
        Partner.builder()
            .name(request.name())
            .taxCode(request.taxCode())
            .email(request.email())
            .phone(request.phone())
            .address(request.address())
            .jobPosition(request.jobPosition())
            .notes(request.notes())
            .partnerType(PartnerType.valueOf(request.partnerType()))
            .organization(organization)
            .build();

    if (request.contacts() != null) {
      List<PartnerContact> contacts = new ArrayList<>();
      for (PartnerContactRequest contactRequest : request.contacts()) {
        PartnerContact contact =
            PartnerContact.builder()
                .name(contactRequest.name())
                .email(contactRequest.email())
                .phone(contactRequest.phone())
                .jobPosition(contactRequest.jobPosition())
                .notes(contactRequest.notes())
                .partner(partner)
                .build();
        contacts.add(contact);
      }
      partner.setContacts(contacts);
    }

    partner = partnerRepository.save(partner);
    log.info(
        "Created partner {} with {} contact(s) in organization {}",
        partner.getId(),
        partner.getContacts().size(),
        organizationId);

    return partnerMapper.toPartnerResponse(partner);
  }

  @Override
  public PagedEntityResponse<PartnerBaseResponse> getPartners(
      UUID organizationId, String search, PaginationRequest paginationRequest) {
    findOrganizationById(organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit());

    Page<UUID> ids =
        (search != null && !search.trim().isEmpty())
            ? partnerRepository.findPartnerIdsByOrganizationIdAndSearch(
                organizationId, search, pageable)
            : partnerRepository.findPartnerIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Partner> partnerMap =
        partnerRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Partner::getId, Function.identity()));

    List<PartnerBaseResponse> responses =
        ids.getContent().stream()
            .map(partnerMap::get)
            .filter(Objects::nonNull)
            .map(partnerMapper::toPartnerBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public PartnerResponse getPartnerById(UUID organizationId, UUID partnerId) {
    Partner partner = findPartnerByIdAndOrganization(partnerId, organizationId);
    return partnerMapper.toPartnerResponse(partner);
  }

  @Override
  @Transactional
  public PartnerResponse updatePartner(
      UUID organizationId, UUID partnerId, UpdatePartnerRequest request) {
    Partner partner = findPartnerByIdAndOrganization(partnerId, organizationId);

    partner.setName(request.name());
    partner.setTaxCode(request.taxCode());
    partner.setEmail(request.email());
    partner.setPhone(request.phone());
    partner.setAddress(request.address());
    partner.setJobPosition(request.jobPosition());
    partner.setNotes(request.notes());
    partner.setPartnerType(PartnerType.valueOf(request.partnerType()));

    List<PartnerContactRequest> contactRequests =
        request.contacts() != null ? request.contacts() : List.of();

    Set<UUID> retainedIds =
        contactRequests.stream()
            .map(PartnerContactRequest::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    partner.getContacts().removeIf(c -> !retainedIds.contains(c.getId()));

    for (PartnerContactRequest contactRequest : contactRequests) {
      if (contactRequest.id() != null) {
        PartnerContact existing =
            partner.getContacts().stream()
                .filter(c -> c.getId().equals(contactRequest.id()))
                .findFirst()
                .orElseThrow(
                    () -> {
                      log.warn(
                          "Contact {} not found for partner {}", contactRequest.id(), partnerId);
                      return new ResourceNotFoundException(
                          "Partner contact not found with id: "
                              + contactRequest.id()
                              + " for partner: "
                              + partnerId);
                    });
        existing.setName(contactRequest.name());
        existing.setEmail(contactRequest.email());
        existing.setPhone(contactRequest.phone());
        existing.setJobPosition(contactRequest.jobPosition());
        existing.setNotes(contactRequest.notes());
        log.debug("Updated existing contact {} for partner {}", existing.getId(), partnerId);
      } else {
        PartnerContact newContact =
            PartnerContact.builder()
                .name(contactRequest.name())
                .email(contactRequest.email())
                .phone(contactRequest.phone())
                .jobPosition(contactRequest.jobPosition())
                .notes(contactRequest.notes())
                .partner(partner)
                .build();
        partner.getContacts().add(newContact);
        log.debug("Created new contact for partner {}", partnerId);
      }
    }

    partner = partnerRepository.save(partner);
    partner = findPartnerByIdAndOrganization(partnerId, organizationId);
    log.info("Updated partner {} in organization {}", partnerId, organizationId);

    return partnerMapper.toPartnerResponse(partner);
  }

  @Override
  @Transactional
  public PartnerResponse updatePartnerArchiveStatus(
      UUID organizationId, UUID partnerId, UpdateArchiveStatusRequest request) {
    Partner partner = findPartnerByIdAndOrganization(partnerId, organizationId);

    partner.setIsArchived(request.isArchived());

    partner = partnerRepository.save(partner);
    log.info(
        "Partner {} archive status set to {} in organization {}",
        partnerId,
        request.isArchived(),
        organizationId);

    return partnerMapper.toPartnerResponse(partner);
  }

  @Override
  @Transactional
  public void deletePartner(UUID organizationId, UUID partnerId) {
    Partner partner = findPartnerByIdAndOrganization(partnerId, organizationId);
    partnerRepository.delete(partner);
    log.info("Deleted partner {} from organization {}", partnerId, organizationId);
  }

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () -> {
              log.warn("Organization with ID {} not found", organizationId);
              return new ResourceNotFoundException(
                  "Organization not found with id: " + organizationId);
            });
  }

  private Partner findPartnerByIdAndOrganization(UUID partnerId, UUID organizationId) {
    return partnerRepository
        .findByIdAndOrganizationId(partnerId, organizationId)
        .orElseThrow(
            () -> {
              log.warn("Partner {} not found in organization {}", partnerId, organizationId);
              return new ResourceNotFoundException(
                  "Partner not found with id: "
                      + partnerId
                      + " in organization: "
                      + organizationId);
            });
  }
}
