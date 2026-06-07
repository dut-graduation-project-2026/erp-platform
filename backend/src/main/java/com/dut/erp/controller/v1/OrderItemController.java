package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.UpsertOrderItemRequest;
import com.dut.erp.dto.response.OrderItemResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrderItemService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for managing order items under an order. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/orders/{orderId}/items")
public class OrderItemController {

  private final OrderItemService orderItemService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<OrderItemResponse>> getOrderItems(
      @PathVariable UUID organizationId,
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderItemService.getOrderItems(organizationId, orderId));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderItemResponse> getOrderItemById(
      @PathVariable UUID organizationId,
      @PathVariable UUID orderId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderItemService.getOrderItemById(organizationId, orderId, id));
  }

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderItemResponse> createOrderItem(
      @PathVariable UUID organizationId,
      @PathVariable UUID orderId,
      @Valid @RequestBody UpsertOrderItemRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(orderItemService.createOrderItem(organizationId, orderId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrderItemResponse> updateOrderItem(
      @PathVariable UUID organizationId,
      @PathVariable UUID orderId,
      @PathVariable UUID id,
      @Valid @RequestBody UpsertOrderItemRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(orderItemService.updateOrderItem(organizationId, orderId, id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteOrderItem(
      @PathVariable UUID organizationId,
      @PathVariable UUID orderId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    orderItemService.deleteOrderItem(organizationId, orderId, id);
    return ResponseEntity.noContent().build();
  }
}
