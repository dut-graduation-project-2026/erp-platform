package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateStockPickingRequest;
import com.dut.erp.dto.request.LandedCostRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.QcInspectionRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockPickingBaseResponse;
import com.dut.erp.dto.response.StockPickingResponse;
import com.dut.erp.enums.PickingType;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.LandedCostsService;
import com.dut.erp.service.QualityControlService;
import com.dut.erp.service.StockPickingService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/pickings")
public class StockPickingController {

  private final StockPickingService stockPickingService;
  private final QualityControlService qualityControlService;
  private final LandedCostsService landedCostsService;

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<StockPickingBaseResponse>> getPickings(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) PickingType pickingType,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        stockPickingService.getPickings(organizationId, pickingType, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockPickingResponse> getPickingById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockPickingService.getPickingById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockPickingResponse> createPicking(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateStockPickingRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(stockPickingService.createPicking(organizationId, request));
  }

  @PutMapping("/{id}/confirm")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockPickingResponse> confirmPicking(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockPickingService.confirmPicking(organizationId, id));
  }

  @PutMapping("/{id}/assign")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockPickingResponse> assignPicking(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockPickingService.assignPicking(organizationId, id));
  }

  @PutMapping("/{id}/complete")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockPickingResponse> completePicking(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockPickingService.completePicking(organizationId, id));
  }

  @PutMapping("/{id}/qc")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockPickingResponse> inspectPicking(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody QcInspectionRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(qualityControlService.inspectPicking(organizationId, id, request));
  }

  @PostMapping("/{id}/receipt-cost")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('pickings:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> allocateLandedCosts(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody LandedCostRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    landedCostsService.allocateLandedCosts(organizationId, id, request);
    return ResponseEntity.noContent().build();
  }
}
