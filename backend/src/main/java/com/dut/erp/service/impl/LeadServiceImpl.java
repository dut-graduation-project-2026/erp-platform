package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateLeadRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateLeadRequest;
import com.dut.erp.dto.response.LeadBaseResponse;
import com.dut.erp.dto.response.LeadResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.Lead;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.Partner;
import com.dut.erp.entity.SaleTeam;
import com.dut.erp.entity.User;
import com.dut.erp.enums.LeadStage;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.LeadMapper;
import com.dut.erp.repository.LeadRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.PartnerRepository;
import com.dut.erp.repository.SaleTeamRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.LeadService;
import com.dut.erp.service.SecurityAuthService;
import com.dut.erp.util.SecurityUtils;
import com.dut.erp.dto.event.LeadAssignedEvent;
import com.dut.erp.dto.event.LeadStageChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
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

import com.dut.erp.repository.PermissionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadServiceImpl implements LeadService {

  private final LeadRepository leadRepository;
  private final OrganizationRepository organizationRepository;
  private final SaleTeamRepository saleTeamRepository;
  private final PartnerRepository partnerRepository;
  private final UserRepository userRepository;
  private final PermissionRepository permissionRepository;
  private final LeadMapper leadMapper;
  private final ApplicationEventPublisher applicationEventPublisher;
  private final SecurityAuthService securityAuthService;

  @Override
  public PagedEntityResponse<LeadBaseResponse> getLeadsWithFilterByOrganizationId(
      UUID organizationId, String search, PaginationRequest paginationRequest) {
    log.info("Fetching leads for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("name"), SortField.asc("updatedAt")));

    CustomUserDetails currentUser = SecurityUtils.getCurrentUser();
    boolean isAllLeads = securityAuthService.isAdmin(currentUser) || 
        permissionRepository.existsByUserIdAndOrganizationIdAndPermissionCode(
            currentUser.getId(), organizationId, "leads:read_all"
        );

    Page<UUID> ids;
    if (isAllLeads) {
      ids = (search != null && !search.trim().isEmpty())
          ? leadRepository.findIdsByOrganizationIdAndSearch(organizationId, search, pageable)
          : leadRepository.findIdsByOrganizationId(organizationId, pageable);
    } else {
      List<UUID> teamIds = saleTeamRepository.findIdsByOrganizationIdAndUserId(organizationId, currentUser.getId());
      if (teamIds.isEmpty()) {
        teamIds = List.of(UUID.fromString("00000000-0000-0000-0000-000000000000"));
      }
      ids = (search != null && !search.trim().isEmpty())
          ? leadRepository.findIdsByOrganizationIdAndSearchAndUser(organizationId, search, currentUser.getId(), teamIds, pageable)
          : leadRepository.findIdsByOrganizationIdAndUser(organizationId, currentUser.getId(), teamIds, pageable);
    }

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, Lead> leadMap =
        leadRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(Lead::getId, Function.identity()));

    List<LeadBaseResponse> responses =
        ids.getContent().stream()
            .map(leadMap::get)
            .filter(Objects::nonNull)
            .map(leadMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public LeadResponse getLeadById(UUID organizationId, UUID leadId) {
    log.info("Fetching lead {} for organization {}", leadId, organizationId);
    Lead lead = findLeadByIdAndOrganizationId(leadId, organizationId);
    securityAuthService.isLeadOwnerOrManagerOrAdmin(lead, SecurityUtils.getCurrentUser());
    return leadMapper.toResponse(lead);
  }

  @Override
  @Transactional
  public LeadResponse createLead(
      UUID organizationId, CreateLeadRequest request, CustomUserDetails userDetails) {
    log.info("Creating lead in organization {}", organizationId);
    Organization organization = findOrganizationById(organizationId);

    SaleTeam saleTeam = validateAndGetSaleTeam(request.saleTeamId(), organizationId);
    User salePerson = validateAndGetSalePerson(request.salePersonId(), saleTeam);
    Partner partner = validateAndGetPartner(request.partnerId(), organizationId);

    Lead lead =
        Lead.builder()
            .organization(organization)
            .name(request.name())
            .taxCode(request.taxCode())
            .email(request.email())
            .phone(request.phone())
            .address(request.address())
            .notes(request.notes())
            .expectedRevenue(request.expectedRevenue())
            .stage(LeadStage.NEW)
            .probability(request.probability())
            .salePerson(salePerson)
            .saleTeam(saleTeam)
            .partner(partner)
            .build();

    lead = leadRepository.save(lead);
    log.info("Created lead {} in organization {}", lead.getId(), organizationId);

    if (lead.getSalePerson() != null) {
      applicationEventPublisher.publishEvent(new LeadAssignedEvent(lead.getId(), lead.getSalePerson().getId()));
    }

    return leadMapper.toResponse(lead);
  }

  @Override
  @Transactional
  public LeadResponse updateLead(UUID organizationId, UUID leadId, UpdateLeadRequest request) {
    log.info("Updating lead {} in organization {}", leadId, organizationId);
    Lead lead = findLeadByIdAndOrganizationId(leadId, organizationId);

    securityAuthService.isLeadOwnerOrManagerOrAdmin(lead, SecurityUtils.getCurrentUser());

    SaleTeam saleTeam = validateAndGetSaleTeam(request.saleTeamId(), organizationId);
    User salePerson = validateAndGetSalePerson(request.salePersonId(), saleTeam);
    Partner partner = validateAndGetPartner(request.partnerId(), organizationId);

    User oldAssignee = lead.getSalePerson();

    lead.setName(request.name());
    lead.setTaxCode(request.taxCode());
    lead.setEmail(request.email());
    lead.setPhone(request.phone());
    lead.setAddress(request.address());
    lead.setNotes(request.notes());
    lead.setExpectedRevenue(request.expectedRevenue());
    lead.setProbability(request.probability());
    lead.setSalePerson(salePerson);
    lead.setSaleTeam(saleTeam);
    lead.setPartner(partner);

    lead = leadRepository.save(lead);
    log.info("Updated lead {} in organization {}", leadId, organizationId);

    if (salePerson != null && (oldAssignee == null || !oldAssignee.getId().equals(salePerson.getId()))) {
      applicationEventPublisher.publishEvent(new LeadAssignedEvent(lead.getId(), salePerson.getId()));
    }

    return leadMapper.toResponse(lead);
  }

  @Override
  @Transactional
  public LeadResponse updateLeadStage(UUID organizationId, UUID leadId, String stage) {
    log.info("Updating stage to {} for lead {} in organization {}", stage, leadId, organizationId);
    Lead lead = findLeadByIdAndOrganizationId(leadId, organizationId);

    securityAuthService.isLeadOwnerOrManagerOrAdmin(lead, SecurityUtils.getCurrentUser());

    try {
      LeadStage leadStage = LeadStage.valueOf(stage);
      lead.setStage(leadStage);
    } catch (IllegalArgumentException e) {
      log.warn("Invalid lead stage value: {}", stage);
      throw new BadRequestException("Invalid lead stage: " + stage);
    }

    lead = leadRepository.save(lead);
    log.info(
        "Updated lead stage to {} for lead {} in organization {}", stage, leadId, organizationId);

    applicationEventPublisher.publishEvent(new LeadStageChangedEvent(lead.getId(), stage));

    return leadMapper.toResponse(lead);
  }

  @Override
  @Transactional
  public void deleteLead(UUID organizationId, UUID leadId) {
    log.info("Deleting lead {} from organization {}", leadId, organizationId);
    Lead lead = findLeadByIdAndOrganizationId(leadId, organizationId);

    if (!securityAuthService.isAdmin(SecurityUtils.getCurrentUser())) {
      if (lead.getSaleTeam() == null || lead.getSaleTeam().getLeader() == null
          || !lead.getSaleTeam().getLeader().getId().equals(SecurityUtils.getCurrentUser().getId())) {
        throw new org.springframework.security.access.AccessDeniedException("Access denied: Only system administrators or sales team leaders can delete leads.");
      }
    }

    leadRepository.delete(lead);
    log.info("Deleted lead {} from organization {}", leadId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private Lead findLeadByIdAndOrganizationId(UUID leadId, UUID organizationId) {
    return leadRepository
        .findByIdAndOrganizationId(leadId, organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Lead not found with id: " + leadId + " in organization: " + organizationId));
  }

  private SaleTeam validateAndGetSaleTeam(UUID saleTeamId, UUID organizationId) {
    return saleTeamRepository
        .findByIdAndOrganizationId(saleTeamId, organizationId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Sale team not found with id: " + saleTeamId));
  }

  private User validateAndGetSalePerson(UUID salePersonId, SaleTeam saleTeam) {
    if (salePersonId == null) {
      return null;
    }
    boolean belongsToTeam =
        saleTeam.getMembers().stream().anyMatch(member -> member.getId().equals(salePersonId));
    if (!belongsToTeam) {
      log.warn("User {} does not belong to sale team {}", salePersonId, saleTeam.getId());
      throw new BadRequestException("User detail must belong to the selected sale team");
    }
    return userRepository
        .findById(salePersonId)
        .orElseThrow(
            () -> new ResourceNotFoundException("User not found with id: " + salePersonId));
  }

  private Partner validateAndGetPartner(UUID partnerId, UUID organizationId) {
    if (partnerId == null) {
      return null;
    }
    return partnerRepository
        .findByIdAndOrganizationId(partnerId, organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "Partner not found with id: "
                        + partnerId
                        + " in organization: "
                        + organizationId));
  }
}
