package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateCrmLeadRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.CrmLeadBaseResponse;
import com.dut.erp.dto.response.CrmLeadResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.CrmLead;
import com.dut.erp.entity.CrmStage;
import com.dut.erp.entity.Organization;
import com.dut.erp.entity.SalePartner;
import com.dut.erp.entity.SalesTeam;
import com.dut.erp.entity.User;
import com.dut.erp.enums.LeadType;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.CrmLeadMapper;
import com.dut.erp.repository.CrmLeadRepository;
import com.dut.erp.repository.CrmStageRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.repository.SalePartnerRepository;
import com.dut.erp.repository.SalesTeamRepository;
import com.dut.erp.repository.UserRepository;
import com.dut.erp.service.CrmLeadService;
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
public class CrmLeadServiceImpl implements CrmLeadService {

  private final OrganizationRepository organizationRepository;
  private final CrmLeadRepository crmLeadRepository;
  private final CrmStageRepository crmStageRepository;
  private final SalePartnerRepository salePartnerRepository;
  private final SalesTeamRepository salesTeamRepository;
  private final UserRepository userRepository;
  private final CrmLeadMapper crmLeadMapper;

  @Override
  public PagedEntityResponse<CrmLeadBaseResponse> getLeadsByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest) {
    log.info("Fetching CRM leads for organization {}", organizationId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("updatedAt"), SortField.asc("name")));

    Page<UUID> ids = crmLeadRepository.findIdsByOrganizationId(organizationId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, CrmLead> leadMap =
        crmLeadRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(CrmLead::getId, Function.identity()));

    List<CrmLeadBaseResponse> responses =
        ids.getContent().stream()
            .map(leadMap::get)
            .filter(Objects::nonNull)
            .map(crmLeadMapper::toBaseResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  public CrmLeadResponse getLeadById(UUID organizationId, UUID leadId) {
    log.info("Fetching CRM lead {} for organization {}", leadId, organizationId);
    CrmLead lead = findLeadByIdAndVerifyOrganization(leadId, organizationId);
    return crmLeadMapper.toResponse(lead);
  }

  @Override
  @Transactional
  public CrmLeadResponse createLead(UUID organizationId, CreateCrmLeadRequest request) {
    Organization organization = findOrganizationById(organizationId);
    CrmStage stage = findStageByIdAndVerifyOrganization(request.stageId(), organizationId);

    CrmLead.CrmLeadBuilder builder =
        CrmLead.builder()
            .organization(organization)
            .name(request.name())
            .type(request.type() != null ? request.type() : LeadType.LEAD)
            .stage(stage)
            .expectedRevenue(request.expectedRevenue())
            .probability(request.probability());

    if (request.partnerId() != null) {
      SalePartner partner =
          salePartnerRepository
              .findById(request.partnerId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Partner not found: " + request.partnerId()));
      builder.partner(partner);
    }

    if (request.salespersonId() != null) {
      User salesperson =
          userRepository
              .findById(request.salespersonId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException("User not found: " + request.salespersonId()));
      builder.salesperson(salesperson);
    }

    if (request.salesTeamId() != null) {
      SalesTeam team =
          salesTeamRepository
              .findById(request.salesTeamId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Sales team not found: " + request.salesTeamId()));
      builder.salesTeam(team);
    }

    CrmLead lead = crmLeadRepository.save(builder.build());
    log.info("Created CRM lead {} in organization {}", lead.getId(), organizationId);
    return crmLeadMapper.toResponse(lead);
  }

  @Override
  @Transactional
  public CrmLeadResponse updateLead(
      UUID organizationId, UUID leadId, CreateCrmLeadRequest request) {
    findOrganizationById(organizationId);
    CrmLead lead = findLeadByIdAndVerifyOrganization(leadId, organizationId);
    CrmStage stage = findStageByIdAndVerifyOrganization(request.stageId(), organizationId);

    lead.setName(request.name());
    lead.setStage(stage);
    if (request.expectedRevenue() != null) lead.setExpectedRevenue(request.expectedRevenue());
    if (request.probability() != null) lead.setProbability(request.probability());

    if (request.type() != null) {
      if (request.type() == LeadType.OPPORTUNITY
          && lead.getPartner() == null
          && request.partnerId() == null) {
        throw new BadRequestException("Opportunity must have an associated partner.");
      }
      lead.setType(request.type());
    }

    if (request.partnerId() != null) {
      SalePartner partner =
          salePartnerRepository
              .findById(request.partnerId())
              .orElseThrow(
                  () -> new ResourceNotFoundException("Partner not found: " + request.partnerId()));
      lead.setPartner(partner);
    }

    if (request.salespersonId() != null) {
      User salesperson =
          userRepository
              .findById(request.salespersonId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException("User not found: " + request.salespersonId()));
      lead.setSalesperson(salesperson);
    }

    if (request.salesTeamId() != null) {
      SalesTeam team =
          salesTeamRepository
              .findById(request.salesTeamId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Sales team not found: " + request.salesTeamId()));
      lead.setSalesTeam(team);
    }

    lead = crmLeadRepository.save(lead);
    log.info("Updated CRM lead {} in organization {}", leadId, organizationId);
    return crmLeadMapper.toResponse(lead);
  }

  @Override
  @Transactional
  public void deleteLead(UUID organizationId, UUID leadId) {
    findOrganizationById(organizationId);
    CrmLead lead = findLeadByIdAndVerifyOrganization(leadId, organizationId);
    crmLeadRepository.delete(lead);
    log.info("Deleted CRM lead {} from organization {}", leadId, organizationId);
  }

  // ---- Private helpers ----

  private Organization findOrganizationById(UUID organizationId) {
    return organizationRepository
        .findById(organizationId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException("Organization not found with id: " + organizationId));
  }

  private CrmLead findLeadByIdAndVerifyOrganization(UUID leadId, UUID organizationId) {
    CrmLead lead =
        crmLeadRepository
            .findByIdWithDetails(leadId)
            .orElseThrow(
                () -> new ResourceNotFoundException("CRM Lead not found with id: " + leadId));
    if (!lead.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Lead does not belong to the specified organization.");
    }
    return lead;
  }

  private CrmStage findStageByIdAndVerifyOrganization(UUID stageId, UUID organizationId) {
    CrmStage stage =
        crmStageRepository
            .findById(stageId)
            .orElseThrow(
                () -> new ResourceNotFoundException("CRM Stage not found with id: " + stageId));
    if (!stage.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Stage does not belong to the specified organization.");
    }
    return stage;
  }
}
