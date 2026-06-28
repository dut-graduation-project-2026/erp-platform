package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateInvoiceRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateInvoiceStatusRequest;
import com.dut.erp.dto.response.InvoiceBaseResponse;
import com.dut.erp.dto.response.InvoiceResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.InvoiceService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/invoices")
public class InvoiceController {

  private final InvoiceService invoiceService;

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('invoices:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<InvoiceResponse> createInvoice(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateInvoiceRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(invoiceService.createInvoiceFromOrder(organizationId, request));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('invoices:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateInvoiceStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(invoiceService.updateInvoiceStatus(organizationId, id, request));
  }

  @PostMapping("/{id}/payments")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('invoices:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InvoiceResponse> registerPayment(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody com.dut.erp.dto.request.RegisterPaymentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(invoiceService.registerPayment(organizationId, id, request));
  }

  @GetMapping("/order/{orderId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('invoices:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<InvoiceResponse> getInvoiceByOrderId(
      @PathVariable UUID organizationId,
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(invoiceService.getInvoiceByOrderId(organizationId, orderId));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('invoices:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<InvoiceResponse> getInvoiceById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(invoiceService.getInvoiceById(organizationId, id));
  }

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('invoices:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<InvoiceBaseResponse>> getInvoices(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        invoiceService.getInvoices(organizationId, search, status, paginationRequest));
  }
}
