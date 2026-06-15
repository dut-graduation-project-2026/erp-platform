package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateWarehouseRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateWarehouseRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.WarehouseBaseResponse;
import com.dut.erp.dto.response.WarehouseResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.WarehouseService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/warehouses")
public class WarehouseController {

  private final WarehouseService warehouseService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<WarehouseBaseResponse>> getWarehouses(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(warehouseService.getWarehouses(organizationId, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<WarehouseResponse> getWarehouseById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(warehouseService.getWarehouseById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<WarehouseResponse> createWarehouse(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateWarehouseRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(warehouseService.createWarehouse(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<WarehouseResponse> updateWarehouse(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateWarehouseRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(warehouseService.updateWarehouse(organizationId, id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteWarehouse(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    warehouseService.deleteWarehouse(organizationId, id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/metrics")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<com.dut.erp.dto.response.WarehouseMetricsResponse> getWarehouseMetrics(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(warehouseService.getWarehouseMetrics(organizationId, id));
  }
}
