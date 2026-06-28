package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateReplenishmentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ReplenishmentRequestResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.ReplenishmentRequestService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/warehouses/{warehouseId}/replenishment-requests")
public class ReplenishmentRequestController {

  private final ReplenishmentRequestService replenishmentRequestService;

  @PostMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<ReplenishmentRequestResponse> createReplenishmentRequest(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @Valid @RequestBody CreateReplenishmentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(replenishmentRequestService.createReplenishmentRequest(organizationId, warehouseId, request));
  }

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<ReplenishmentRequestResponse>> getReplenishmentRequests(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        replenishmentRequestService.getReplenishmentRequests(organizationId, warehouseId, search, status, paginationRequest));
  }
}
