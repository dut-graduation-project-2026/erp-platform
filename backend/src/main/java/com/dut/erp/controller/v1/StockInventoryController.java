package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateStockInventoryRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.SubmitInventoryCountRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockInventoryBaseResponse;
import com.dut.erp.dto.response.StockInventoryResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.StockInventoryService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/inventory")
public class StockInventoryController {

  private final StockInventoryService stockInventoryService;

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('inventory:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<StockInventoryBaseResponse>> getInventories(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        stockInventoryService.getInventories(organizationId, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('inventory:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockInventoryResponse> getInventoryById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockInventoryService.getInventoryById(organizationId, id));
  }

  @PostMapping("/create")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('inventory:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockInventoryResponse> createInventory(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateStockInventoryRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(stockInventoryService.createInventory(organizationId, request));
  }

  @PutMapping("/{id}/start")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('inventory:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockInventoryResponse> startInventory(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockInventoryService.startInventory(organizationId, id));
  }

  @PutMapping("/{id}/submit")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('inventory:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockInventoryResponse> submitCounts(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody SubmitInventoryCountRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockInventoryService.submitCounts(organizationId, id, request));
  }

  @PutMapping("/{id}/validate")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('inventory:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockInventoryResponse> validateInventory(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockInventoryService.validateInventory(organizationId, id));
  }
}
