package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateOrderStatusRequest;
import com.dut.erp.dto.response.OrderBaseResponse;
import com.dut.erp.dto.response.OrderResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.enums.OrderStatus;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrderService;
import com.dut.erp.service.SalesOrderIntegrationService;
import com.dut.erp.dto.request.ConfirmRouteRequest;
import com.dut.erp.dto.response.RouteProposalResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

/** Controller for managing Sales Orders (non-DRAFT) within an organization. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/orders")
public class OrderController {

  private final OrderService orderService;
  private final SalesOrderIntegrationService salesOrderIntegrationService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<OrderBaseResponse>> getOrders(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) OrderStatus status,
      @RequestParam(required = false) UUID partnerId,
      @RequestParam(required = false) UUID salePersonId,
      @RequestParam(required = false) UUID saleTeamId,
      @RequestParam(required = false) Instant startDate,
      @RequestParam(required = false) Instant endDate,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    Instant finalEndDate = endDate;
    if (startDate != null && endDate == null) {
      finalEndDate = Instant.now();
    }
    return ResponseEntity.ok(
        orderService.getOrdersWithFilterByOrganizationId(
            organizationId,
            search,
            status,
            partnerId,
            salePersonId,
            saleTeamId,
            startDate,
            finalEndDate,
            paginationRequest));
  }

  @GetMapping("/confirmed")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<OrderBaseResponse>> getConfirmedOrders(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        orderService.getOrdersByStatus(
            organizationId, com.dut.erp.enums.OrderStatus.CONFIRMED, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderResponse> getOrderById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderService.getOrderById(organizationId, id));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderResponse> updateOrderStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateOrderStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderService.updateOrderStatus(organizationId, id, request));
  }

  @GetMapping("/smart-route/preview")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<RouteProposalResponse>> previewSmartRoute(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) java.util.UUID warehouseId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(salesOrderIntegrationService.previewSmartRoute(organizationId, warehouseId));
  }

  @PostMapping("/smart-route/confirm")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> confirmSmartRoute(
      @PathVariable UUID organizationId,
      @Valid @RequestBody ConfirmRouteRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    salesOrderIntegrationService.confirmSmartRoute(organizationId, request);
    return ResponseEntity.ok().build();
  }
}
