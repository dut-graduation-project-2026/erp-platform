package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateArchiveStatusRequest;
import com.dut.erp.dto.request.UpsertTaxRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.TaxBaseResponse;
import com.dut.erp.dto.response.TaxResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.TaxService;
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

/** Controller for managing taxes within an organization. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/taxes")
public class TaxController {

  private final TaxService taxService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('taxes:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<TaxBaseResponse>> getTaxes(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "false") Boolean isArchived,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        taxService.getTaxesWithFilterByOrganizationId(
            organizationId, search, isArchived, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('taxes:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<TaxResponse> getTaxById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(taxService.getTaxById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('taxes:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<TaxResponse> createTax(
      @PathVariable UUID organizationId,
      @Valid @RequestBody UpsertTaxRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(taxService.createTax(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('taxes:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<TaxResponse> updateTax(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpsertTaxRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(taxService.updateTax(organizationId, id, request));
  }

  @PatchMapping("/{id}/archive-status")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('taxes:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<TaxResponse> updateTaxArchiveStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateArchiveStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        taxService.updateTaxArchiveStatus(organizationId, id, request.isArchived()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('taxes:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteTax(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    taxService.deleteTax(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
