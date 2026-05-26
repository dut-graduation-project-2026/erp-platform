package com.dut.erp.service;

import com.dut.erp.dto.request.CreateCrmAppointmentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.CrmAppointmentResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import java.util.UUID;

public interface CrmAppointmentService {

  PagedEntityResponse<CrmAppointmentResponse> getAppointmentsByOrganizationId(
      UUID organizationId, UUID leadId, PaginationRequest paginationRequest);

  CrmAppointmentResponse createAppointment(
      UUID organizationId, CreateCrmAppointmentRequest request);

  CrmAppointmentResponse updateAppointmentStatus(
      UUID organizationId, UUID appointmentId, com.dut.erp.enums.AppointmentStatus status);

  void deleteAppointment(UUID organizationId, UUID appointmentId);
}
