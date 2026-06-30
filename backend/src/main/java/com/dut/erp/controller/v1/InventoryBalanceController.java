package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.InventoryBalanceBaseResponse;
import com.dut.erp.dto.response.InventoryBalanceResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.InventoryBalanceService;
import jakarta.validation.Valid;
import com.dut.erp.dto.response.StockLayerResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/warehouses/{warehouseId}/balances")
public class InventoryBalanceController {

  private final InventoryBalanceService inventoryBalanceService;

  /**
   * GET /api/v1/organizations/{organizationId}/warehouses/{warehouseId}/balances
   * Returns a paginated list of inventory balances in the given warehouse.
   * Supports optional ?search= query param to filter by product name.
   */
  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<InventoryBalanceBaseResponse>> getBalances(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @RequestParam(required = false) String search,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        inventoryBalanceService.getBalancesByWarehouse(
            organizationId, warehouseId, search, paginationRequest));
  }

  /**
   * GET /api/v1/organizations/{organizationId}/warehouses/{warehouseId}/balances/{id}
   * Returns the detail of a single inventory balance record.
   */
  @GetMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryBalanceResponse> getBalanceById(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        inventoryBalanceService.getBalanceById(organizationId, warehouseId, id));
  }

  /**
   * GET /api/v1/organizations/{organizationId}/warehouses/{warehouseId}/balances/products/{productId}/layers
   * Returns the list of active stock layers (virtual lots) for a product in a warehouse.
   */
  @GetMapping("/products/{productId}/layers")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<StockLayerResponse>> getActiveLayers(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID productId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        inventoryBalanceService.getActiveLayers(organizationId, warehouseId, productId));
  }
}
