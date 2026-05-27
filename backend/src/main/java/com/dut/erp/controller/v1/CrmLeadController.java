package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateCrmLeadRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.CrmLeadBaseResponse;
import com.dut.erp.dto.response.CrmLeadResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.CrmLeadService;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing CRM leads and opportunities within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/crm/leads")
public class CrmLeadController {

  private final CrmLeadService crmLeadService;

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<CrmLeadBaseResponse>> getLeads(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        crmLeadService.getLeadsByOrganizationId(organizationId, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<CrmLeadResponse> getLeadById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(crmLeadService.getLeadById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<CrmLeadResponse> createLead(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateCrmLeadRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(crmLeadService.createLead(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<CrmLeadResponse> updateLead(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody CreateCrmLeadRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(crmLeadService.updateLead(organizationId, id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('crm:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteLead(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    crmLeadService.deleteLead(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
