package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateLeadRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateLeadRequest;
import com.dut.erp.dto.request.UpdateLeadStageRequest;
import com.dut.erp.dto.response.LeadBaseResponse;
import com.dut.erp.dto.response.LeadResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.LeadService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling lead management API endpoints. Provides endpoints for creating,
 * retrieving, updating, updating stage, and deleting leads within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/leads")
public class LeadController {

  private final LeadService leadService;

  /**
   * Creates a new lead within the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param request the lead creation details
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the created LeadResponse with HTTP 201
   */
  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<LeadResponse> createLead(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateLeadRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    LeadResponse response = leadService.createLead(organizationId, request, userDetails);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves leads belonging to the specified organization with filtering and pagination.
   *
   * @param organizationId the UUID of the organization
   * @param search an optional search term for filtering by name, email, or phone
   * @param paginationRequest pagination options
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing a paged list of LeadBaseResponse objects
   */
  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<LeadBaseResponse>> getLeads(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PagedEntityResponse<LeadBaseResponse> response =
        leadService.getLeadsWithFilterByOrganizationId(organizationId, search, paginationRequest);
    return ResponseEntity.ok(response);
  }

  /**
   * Retrieves a single lead by its ID within the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param leadId the UUID of the lead to retrieve
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the LeadResponse
   */
  @GetMapping("/{leadId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<LeadResponse> getLeadById(
      @PathVariable UUID organizationId,
      @PathVariable UUID leadId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    LeadResponse response = leadService.getLeadById(organizationId, leadId);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates an existing lead.
   *
   * @param organizationId the UUID of the organization
   * @param leadId the UUID of the lead to update
   * @param request the update request containing new lead details
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the updated LeadResponse
   */
  @PutMapping("/{leadId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<LeadResponse> updateLead(
      @PathVariable UUID organizationId,
      @PathVariable UUID leadId,
      @Valid @RequestBody UpdateLeadRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    LeadResponse response = leadService.updateLead(organizationId, leadId, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates the stage of an existing lead.
   *
   * @param organizationId the UUID of the organization
   * @param leadId the UUID of the lead
   * @param request the lead stage update request
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the updated LeadResponse
   */
  @PatchMapping("/{leadId}/stage")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<LeadResponse> updateLeadStage(
      @PathVariable UUID organizationId,
      @PathVariable UUID leadId,
      @Valid @RequestBody UpdateLeadStageRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    LeadResponse response = leadService.updateLeadStage(organizationId, leadId, request.stage());
    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a lead by its ID within the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param leadId the UUID of the lead to delete
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity with HTTP 204 No Content
   */
  @DeleteMapping("/{leadId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('leads:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteLead(
      @PathVariable UUID organizationId,
      @PathVariable UUID leadId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    leadService.deleteLead(organizationId, leadId);
    return ResponseEntity.noContent().build();
  }
}
