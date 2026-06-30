package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreatePartnerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateArchiveStatusRequest;
import com.dut.erp.dto.request.UpdatePartnerRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.PartnerBaseResponse;
import com.dut.erp.dto.response.PartnerResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.PartnerService;
import jakarta.validation.Valid;
import java.util.List;
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
 * Controller handling partner management API endpoints. Provides endpoints for creating,
 * retrieving, updating, archiving, and deleting partners within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/partners")
public class PartnerController {

  private final PartnerService partnerService;

  /**
   * Creates a new partner within the specified organization.
   *
   * <p>The authenticated user must have organization access and {@code partners:write} permission.
   *
   * @param organizationId the UUID of the organization
   * @param request the partner creation details
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the created PartnerResponse with HTTP 201
   */
  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('partners:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<PartnerResponse> createPartner(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreatePartnerRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PartnerResponse response = partnerService.createPartner(organizationId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Retrieves all partners belonging to the specified organization.
   *
   * <p>The authenticated user must have organization access and {@code partners:read} permission.
   *
   * @param organizationId the UUID of the organization
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing a list of PartnerResponse objects
   */
  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('partners:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<PartnerBaseResponse>> getPartners(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PagedEntityResponse<PartnerBaseResponse> responses =
        partnerService.getPartners(organizationId, search, paginationRequest);
    return ResponseEntity.ok(responses);
  }

  /**
   * Retrieves a single partner by its ID within the specified organization.
   *
   * <p>The authenticated user must have organization access and {@code partners:select} permission.
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner to retrieve
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the PartnerResponse
   */
  @GetMapping("/{partnerId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('partners:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PartnerResponse> getPartnerById(
      @PathVariable UUID organizationId,
      @PathVariable UUID partnerId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PartnerResponse response = partnerService.getPartnerById(organizationId, partnerId);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates an existing partner.
   *
   * <p>The authenticated user must have organization access and {@code partners:write} permission.
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner to update
   * @param request the update request containing new partner details
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the updated PartnerResponse
   */
  @PutMapping("/{partnerId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('partners:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<PartnerResponse> updatePartner(
      @PathVariable UUID organizationId,
      @PathVariable UUID partnerId,
      @Valid @RequestBody UpdatePartnerRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PartnerResponse response = partnerService.updatePartner(organizationId, partnerId, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Updates the archive status of a partner (archive or unarchive).
   *
   * <p>Uses PATCH semantics since only a single field is being modified. The authenticated user
   * must have organization access and {@code partners:write} permission.
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner
   * @param request the archive status update request containing the new {@code isArchived} value
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the updated PartnerResponse
   */
  @PatchMapping("/{partnerId}/archive-status")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('partners:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<PartnerResponse> updatePartnerArchiveStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID partnerId,
      @Valid @RequestBody UpdateArchiveStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PartnerResponse response =
        partnerService.updatePartnerArchiveStatus(organizationId, partnerId, request);
    return ResponseEntity.ok(response);
  }

  /**
   * Deletes a partner by its ID within the specified organization.
   *
   * <p>The authenticated user must have organization access and {@code partners:write} permission.
   *
   * @param organizationId the UUID of the organization
   * @param partnerId the UUID of the partner to delete
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity with HTTP 204 No Content
   */
  @DeleteMapping("/{partnerId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('partners:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deletePartner(
      @PathVariable UUID organizationId,
      @PathVariable UUID partnerId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    partnerService.deletePartner(organizationId, partnerId);
    return ResponseEntity.noContent().build();
  }
}
