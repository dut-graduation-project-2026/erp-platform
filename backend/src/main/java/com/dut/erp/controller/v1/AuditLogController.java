package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.AuditLogResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.AuditLogService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling audit log retrieval. Exposes an endpoint to retrieve audit logs for a
 * specific entity (by ID) within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/{assetType}/{assetId}/logs")
public class AuditLogController {

  private final AuditLogService auditLogService;

  /**
   * Retrieves a paginated list of audit logs for a specific entity within the organization.
   *
   * @param organizationId the UUID of the organization
   * @param assetType the type of the asset
   * @param assetId the UUID of the asset
   * @param paginationRequest the pagination parameters (page and limit)
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the paged response of AuditLogResponse objects
   */
  @GetMapping
  @PreAuthorize("@securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)")
  public ResponseEntity<PagedEntityResponse<AuditLogResponse>> getAuditLogsByEntity(
      @PathVariable UUID organizationId,
      @PathVariable String assetType,
      @PathVariable UUID assetId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PagedEntityResponse<AuditLogResponse> response =
        auditLogService.getAuditLogsByEntityId(organizationId, assetId, paginationRequest);
    return ResponseEntity.ok(response);
  }
}
