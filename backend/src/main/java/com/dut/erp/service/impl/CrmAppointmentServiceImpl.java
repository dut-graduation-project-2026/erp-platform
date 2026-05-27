package com.dut.erp.service.impl;

import com.dut.erp.constant.SortingConstants;
import com.dut.erp.dto.common.SortField;
import com.dut.erp.dto.request.CreateCrmAppointmentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.CrmAppointmentResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.entity.CrmAppointment;
import com.dut.erp.entity.CrmLead;
import com.dut.erp.entity.Organization;
import com.dut.erp.enums.AppointmentStatus;
import com.dut.erp.exception.BadRequestException;
import com.dut.erp.exception.ResourceNotFoundException;
import com.dut.erp.mapper.CrmAppointmentMapper;
import com.dut.erp.repository.CrmAppointmentRepository;
import com.dut.erp.repository.CrmLeadRepository;
import com.dut.erp.repository.OrganizationRepository;
import com.dut.erp.service.CrmAppointmentService;
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
public class CrmAppointmentServiceImpl implements CrmAppointmentService {

  private final OrganizationRepository organizationRepository;
  private final CrmLeadRepository crmLeadRepository;
  private final CrmAppointmentRepository crmAppointmentRepository;
  private final CrmAppointmentMapper crmAppointmentMapper;

  @Override
  public PagedEntityResponse<CrmAppointmentResponse> getAppointmentsByOrganizationId(
      UUID organizationId, UUID leadId, PaginationRequest paginationRequest) {
    log.info("Fetching appointments for organization {} and lead {}", organizationId, leadId);

    Pageable pageable =
        PageRequest.of(
            paginationRequest.page() - 1,
            paginationRequest.limit(),
            SortingConstants.customEntitiesSort(SortField.asc("startTime")));

    Page<UUID> ids = crmAppointmentRepository.findIdsByOrganizationIdAndLeadId(organizationId, leadId, pageable);

    if (ids.isEmpty()) {
      return PagedEntityResponse.from(Page.empty(pageable));
    }

    Map<UUID, CrmAppointment> appointmentMap =
        crmAppointmentRepository.findAllByIdIn(ids.getContent()).stream()
            .collect(Collectors.toMap(CrmAppointment::getId, Function.identity()));

    List<CrmAppointmentResponse> responses =
        ids.getContent().stream()
            .map(appointmentMap::get)
            .filter(Objects::nonNull)
            .map(crmAppointmentMapper::toResponse)
            .collect(Collectors.toList());

    return PagedEntityResponse.from(new PageImpl<>(responses, pageable, ids.getTotalElements()));
  }

  @Override
  @Transactional
  public CrmAppointmentResponse createAppointment(
      UUID organizationId, CreateCrmAppointmentRequest request) {
    Organization organization = findOrganizationById(organizationId);
    CrmLead lead = findLeadByIdAndVerifyOrganization(request.leadId(), organizationId);

    if (request.endTime().isBefore(request.startTime())) {
      throw new BadRequestException("End time must be after start time.");
    }

    CrmAppointment appointment =
        CrmAppointment.builder()
            .organization(organization)
            .lead(lead)
            .type(request.type())
            .title(request.title())
            .description(request.description())
            .startTime(request.startTime())
            .endTime(request.endTime())
            .location(request.location())
            .build();

    appointment = crmAppointmentRepository.save(appointment);
    log.info("Created appointment {} for lead {}", appointment.getId(), request.leadId());
    return crmAppointmentMapper.toResponse(appointment);
  }

  @Override
  @Transactional
  public CrmAppointmentResponse updateAppointmentStatus(
      UUID organizationId, UUID appointmentId, AppointmentStatus status) {
    findOrganizationById(organizationId);
    CrmAppointment appointment = findAppointmentByIdAndVerifyOrganization(appointmentId, organizationId);
    appointment.setStatus(status);
    appointment = crmAppointmentRepository.save(appointment);
    log.info("Updated appointment {} status to {}", appointmentId, status);
    return crmAppointmentMapper.toResponse(appointment);
  }

  @Override
  @Transactional
  public void deleteAppointment(UUID organizationId, UUID appointmentId) {
    findOrganizationById(organizationId);
    CrmAppointment appointment = findAppointmentByIdAndVerifyOrganization(appointmentId, organizationId);
    crmAppointmentRepository.delete(appointment);
    log.info("Deleted appointment {} from organization {}", appointmentId, organizationId);
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

  private CrmLead findLeadByIdAndVerifyOrganization(UUID leadId, UUID organizationId) {
    CrmLead lead =
        crmLeadRepository
            .findById(leadId)
            .orElseThrow(
                () -> new ResourceNotFoundException("CRM Lead not found with id: " + leadId));
    if (!lead.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Lead does not belong to the specified organization.");
    }
    return lead;
  }

  private CrmAppointment findAppointmentByIdAndVerifyOrganization(
      UUID appointmentId, UUID organizationId) {
    CrmAppointment appointment =
        crmAppointmentRepository
            .findById(appointmentId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Appointment not found with id: " + appointmentId));
    if (!appointment.getOrganization().getId().equals(organizationId)) {
      throw new BadRequestException("Appointment does not belong to the specified organization.");
    }
    return appointment;
  }
}
