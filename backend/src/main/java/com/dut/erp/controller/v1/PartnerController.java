package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateSalePartnerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateSalePartnerRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SalePartnerBaseResponse;
import com.dut.erp.dto.response.SalePartnerResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SalePartnerService;
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
 * Controller handling sale partners (customers and business partners) within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/sale-partners")
public class PartnerController {

  private final SalePartnerService salePartnerService;

  /**
   * Retrieves a paginated list of sale partners for the specified organization.
   */
  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_partners:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<SalePartnerBaseResponse>> getPartners(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        salePartnerService.getPartnersByOrganizationId(organizationId, paginationRequest));
  }

  /**
   * Retrieves the details of a specific sale partner.
   */
  @GetMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_partners:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<SalePartnerResponse> getPartnerById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(salePartnerService.getPartnerById(organizationId, id));
  }

  /**
   * Creates a new sale partner in the specified organization.
   */
  @PostMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_partners:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<SalePartnerResponse> createPartner(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateSalePartnerRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(salePartnerService.createPartner(organizationId, request));
  }

  /**
   * Updates an existing sale partner.
   */
  @PutMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_partners:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<SalePartnerResponse> updatePartner(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateSalePartnerRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(salePartnerService.updatePartner(organizationId, id, request));
  }

  /**
   * Deletes a sale partner from the organization.
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_partners:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deletePartner(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    salePartnerService.deletePartner(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
