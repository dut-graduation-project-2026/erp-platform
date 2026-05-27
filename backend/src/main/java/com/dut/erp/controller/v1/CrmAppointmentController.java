package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateCrmAppointmentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.CrmAppointmentResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.enums.AppointmentStatus;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.CrmAppointmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing CRM appointments (meetings, calls, demos) within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/crm/appointments")
public class CrmAppointmentController {

  private final CrmAppointmentService crmAppointmentService;

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<CrmAppointmentResponse>> getAppointments(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) UUID leadId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        crmAppointmentService.getAppointmentsByOrganizationId(organizationId, leadId, paginationRequest));
  }

  @PostMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<CrmAppointmentResponse> createAppointment(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateCrmAppointmentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(crmAppointmentService.createAppointment(organizationId, request));
  }

  @PutMapping("/{id}/status")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<CrmAppointmentResponse> updateAppointmentStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @RequestParam AppointmentStatus status,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        crmAppointmentService.updateAppointmentStatus(organizationId, id, status));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteAppointment(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    crmAppointmentService.deleteAppointment(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
