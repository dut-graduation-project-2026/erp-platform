package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateSaleInvoiceRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.RegisterPaymentRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleInvoiceBaseResponse;
import com.dut.erp.dto.response.SaleInvoiceResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SaleInvoiceService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing sale invoices and payment registration within an organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/sale-invoices")
public class SaleInvoiceController {

  private final SaleInvoiceService saleInvoiceService;

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('finance:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<SaleInvoiceBaseResponse>> getInvoices(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        saleInvoiceService.getInvoicesByOrganizationId(organizationId, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('finance:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleInvoiceResponse> getInvoiceById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleInvoiceService.getInvoiceById(organizationId, id));
  }

  /**
   * Creates an invoice from a CONFIRMED sale order.
   */
  @PostMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('finance:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleInvoiceResponse> createInvoice(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateSaleInvoiceRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(saleInvoiceService.createInvoice(organizationId, request));
  }

  /**
   * Posts a DRAFT invoice (marks it as POSTED / official).
   */
  @PostMapping("/{id}/post")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('finance:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleInvoiceResponse> postInvoice(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleInvoiceService.postInvoice(organizationId, id));
  }

  /**
   * Registers a payment against an invoice, auto-updating status to PARTIAL_PAID or PAID.
   */
  @PostMapping("/{id}/register-payment")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('finance:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleInvoiceResponse> registerPayment(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody RegisterPaymentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleInvoiceService.registerPayment(organizationId, id, request));
  }

  /**
   * Cancels an invoice (only if not already PAID).
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('finance:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> cancelInvoice(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    saleInvoiceService.cancelInvoice(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
