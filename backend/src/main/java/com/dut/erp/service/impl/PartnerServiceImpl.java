package com.dut.erp.service.impl;

import com.dut.erp.dto.request.CreatePartnerRequest;
import com.dut.erp.dto.request.PartnerContactRequest;
import com.dut.erp.dto.request.UpdatePartnerArchiveStatusRequest;
import com.dut.erp.dto.request.UpdatePartnerRequest;
import com.dut.erp.dto.response.PartnerResponse;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Partner;
import com.dut.erp.entity.PartnerContact;
import com.dut.erp.enums.ActionType;
import com.dut.erp.enums.EntityType;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.PartnerMapper;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PartnerContactRepository;
import com.dut.erp.repository.PartnerRepository;
import com.dut.erp.service.AuditLogService;
import com.dut.erp.service.PartnerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerServiceImpl implements PartnerService {

  private final PartnerRepository partnerRepository;
  private final PartnerContactRepository partnerContactRepository;
  private final OrganizationRepository organizationRepository;
  private final PartnerMapper partnerMapper;
  private final AuditLogService auditLogService;

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
            .partnerType(request.partnerType())
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

    auditLogService.record(
        organizationId,
        EntityType.PARTNER,
        partner.getId(),
        ActionType.CREATE,
        "Created partner: " + partner.getName());

    return partnerMapper.toPartnerResponse(partner);
  }


  @Override
  public List<PartnerResponse> getPartners(UUID organizationId) {
    findOrganizationById(organizationId);
    return partnerRepository.findAllByOrganizationId(organizationId).stream()
        .map(partnerMapper::toPartnerResponse)
        .toList();
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

    List<String> changes = new ArrayList<>();
    if (!Objects.equals(partner.getName(), request.name())) {
      changes.add("name: '" + partner.getName() + "' -> '" + request.name() + "'");
    }
    if (!Objects.equals(partner.getTaxCode(), request.taxCode())) {
      changes.add("taxCode: '" + partner.getTaxCode() + "' -> '" + request.taxCode() + "'");
    }
    if (!Objects.equals(partner.getEmail(), request.email())) {
      changes.add("email: '" + partner.getEmail() + "' -> '" + request.email() + "'");
    }
    if (!Objects.equals(partner.getPhone(), request.phone())) {
      changes.add("phone: '" + partner.getPhone() + "' -> '" + request.phone() + "'");
    }
    if (!Objects.equals(partner.getAddress(), request.address())) {
      changes.add("address: '" + partner.getAddress() + "' -> '" + request.address() + "'");
    }
    if (!Objects.equals(partner.getJobPosition(), request.jobPosition())) {
      changes.add("jobPosition: '" + partner.getJobPosition() + "' -> '" + request.jobPosition() + "'");
    }
    if (!Objects.equals(partner.getNotes(), request.notes())) {
      changes.add("notes: '" + partner.getNotes() + "' -> '" + request.notes() + "'");
    }
    if (!Objects.equals(partner.getPartnerType(), request.partnerType())) {
      changes.add("partnerType: '" + partner.getPartnerType() + "' -> '" + request.partnerType() + "'");
    }

    String message = "Updated partner: " + request.name();
    String changedField = null;
    String oldValue = null;
    String newValue = null;

    if (!changes.isEmpty()) {
      message += ". Changes: " + String.join(", ", changes);
      String firstChange = changes.get(0);
      int colonIdx = firstChange.indexOf(":");
      int arrowIdx = firstChange.indexOf(" -> ");
      if (colonIdx > 0 && arrowIdx > colonIdx) {
        changedField = firstChange.substring(0, colonIdx).trim();
        oldValue = firstChange.substring(colonIdx + 1, arrowIdx).replace("'", "").trim();
        newValue = firstChange.substring(arrowIdx + 4).replace("'", "").trim();
      }
    } else {
      message += ". No fields changed.";
    }

    partner.setName(request.name());
    partner.setTaxCode(request.taxCode());
    partner.setEmail(request.email());
    partner.setPhone(request.phone());
    partner.setAddress(request.address());
    partner.setJobPosition(request.jobPosition());
    partner.setNotes(request.notes());
    partner.setPartnerType(request.partnerType());

    List<PartnerContactRequest> contactRequests =
        request.contacts() != null ? request.contacts() : List.of();

    Set<UUID> retainedIds =
        contactRequests.stream()
            .map(PartnerContactRequest::id)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    List<PartnerContact> existingContacts =
        partnerContactRepository.findAllByPartnerId(partnerId);
    List<PartnerContact> toDelete =
        existingContacts.stream()
            .filter(c -> !retainedIds.contains(c.getId()))
            .toList();
    if (!toDelete.isEmpty()) {
      partnerContactRepository.deleteAll(toDelete);
      log.debug(
          "Deleted {} orphan contact(s) from partner {}", toDelete.size(), partnerId);
    }

    for (PartnerContactRequest contactRequest : contactRequests) {
      if (contactRequest.id() != null) {
        PartnerContact existing =
            partnerContactRepository
                .findByIdAndPartnerId(contactRequest.id(), partnerId)
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
        partnerContactRepository.save(existing);
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
        partnerContactRepository.save(newContact);
        log.debug("Created new contact for partner {}", partnerId);
      }
    }

    partner = partnerRepository.save(partner);
    partner = findPartnerByIdAndOrganization(partnerId, organizationId);
    log.info("Updated partner {} in organization {}", partnerId, organizationId);

    auditLogService.record(
        organizationId,
        EntityType.PARTNER,
        partner.getId(),
        ActionType.UPDATE,
        changedField,
        oldValue,
        newValue,
        message);

    return partnerMapper.toPartnerResponse(partner);
  }

  @Override
  @Transactional
  public PartnerResponse updatePartnerArchiveStatus(
      UUID organizationId, UUID partnerId, UpdatePartnerArchiveStatusRequest request) {
    Partner partner = findPartnerByIdAndOrganization(partnerId, organizationId);

    boolean oldVal = partner.getIsArchived() != null ? partner.getIsArchived() : false;
    partner.setIsArchived(request.isArchived());

    partner = partnerRepository.save(partner);
    log.info(
        "Partner {} archive status set to {} in organization {}",
        partnerId,
        request.isArchived(),
        organizationId);

    auditLogService.record(
        organizationId,
        EntityType.PARTNER,
        partner.getId(),
        ActionType.UPDATE,
        "isArchived",
        String.valueOf(oldVal),
        String.valueOf(request.isArchived()),
        "Updated partner archive status to: " + request.isArchived());

    return partnerMapper.toPartnerResponse(partner);
  }

  @Override
  @Transactional
  public void deletePartner(UUID organizationId, UUID partnerId) {
    Partner partner = findPartnerByIdAndOrganization(partnerId, organizationId);
    partnerRepository.delete(partner);
    log.info("Deleted partner {} from organization {}", partnerId, organizationId);

    auditLogService.record(
        organizationId,
        EntityType.PARTNER,
        partnerId,
        ActionType.DELETE,
        "Deleted partner: " + partner.getName());
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
