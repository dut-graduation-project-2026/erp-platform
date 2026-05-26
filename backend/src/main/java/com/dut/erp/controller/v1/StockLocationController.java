package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateStockLocationRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateStockLocationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockLocationBaseResponse;
import com.dut.erp.dto.response.StockLocationResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.StockLocationService;
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
@RequestMapping("/api/v1/organizations/{organizationId}")
public class StockLocationController {

  private final StockLocationService stockLocationService;

  @GetMapping("/locations")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('locations:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<StockLocationBaseResponse>> getLocations(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        stockLocationService.getLocationsByOrganizationId(organizationId, paginationRequest));
  }

  @GetMapping("/warehouses/{warehouseId}/locations")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('locations:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<StockLocationBaseResponse>> getWarehouseLocations(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        stockLocationService.getLocationsByWarehouseId(organizationId, warehouseId, paginationRequest));
  }

  @GetMapping("/locations/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('locations:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockLocationResponse> getLocationById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(stockLocationService.getLocationById(organizationId, id));
  }

  @PostMapping("/locations")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('locations:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockLocationResponse> createLocation(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateStockLocationRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(stockLocationService.createLocation(organizationId, request));
  }

  @PutMapping("/locations/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('locations:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockLocationResponse> updateLocation(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateStockLocationRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(stockLocationService.updateLocation(organizationId, id, request));
  }

  @DeleteMapping("/locations/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('locations:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteLocation(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    stockLocationService.deleteLocation(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
