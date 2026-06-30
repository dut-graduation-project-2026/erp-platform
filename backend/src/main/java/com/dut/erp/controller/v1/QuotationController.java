package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateOrderStatusRequest;
import com.dut.erp.dto.request.UpsertOrderRequest;
import com.dut.erp.dto.response.OrderBaseResponse;
import com.dut.erp.dto.response.OrderResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrderService;
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

/** Controller for managing Quotations (DRAFT status) within an organization. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/quotations")
public class QuotationController {

  private final OrderService orderService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<OrderBaseResponse>> getQuotations(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) UUID saleTeamId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        orderService.getQuotationsWithFilterByOrganizationId(
            organizationId, search, saleTeamId, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderResponse> getQuotationById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderService.getQuotationById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderResponse> createQuotation(
      @PathVariable UUID organizationId,
      @Valid @RequestBody UpsertOrderRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(orderService.createQuotation(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderResponse> updateQuotation(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpsertOrderRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderService.updateQuotation(organizationId, id, request));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderResponse> updateQuotationStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOrderStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderService.updateOrderStatus(organizationId, id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteQuotation(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    orderService.deleteQuotation(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
