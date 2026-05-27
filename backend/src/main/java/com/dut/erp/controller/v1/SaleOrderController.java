package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateSaleOrderRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleOrderBaseResponse;
import com.dut.erp.dto.response.SaleOrderResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SaleOrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing sale quotations and orders within an organization. DRAFT orders are
 * Quotations; CONFIRMED orders are Sale Orders.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/sale-orders")
public class SaleOrderController {

  private final SaleOrderService saleOrderService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sales:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<SaleOrderBaseResponse>> getOrders(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        saleOrderService.getOrdersByOrganizationId(organizationId, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sales:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleOrderResponse> getOrderById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleOrderService.getOrderById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sales:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleOrderResponse> createOrder(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateSaleOrderRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(saleOrderService.createOrder(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sales:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleOrderResponse> updateOrder(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody CreateSaleOrderRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleOrderService.updateOrder(organizationId, id, request));
  }
}
