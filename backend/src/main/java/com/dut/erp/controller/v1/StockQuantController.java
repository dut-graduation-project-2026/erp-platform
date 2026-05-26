package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.StockQuantResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.StockQuantService;
import com.dut.erp.service.PickingOptimalService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
@RequestMapping("/api/v1/organizations/{organizationId}/stock/quants")
public class StockQuantController {

  private final StockQuantService stockQuantService;
  private final PickingOptimalService pickingOptimalService;

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('quants:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<StockQuantResponse>> getStockQuants(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) UUID productId,
      @RequestParam(required = false) UUID warehouseId,
      @RequestParam(required = false) UUID locationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        stockQuantService.getStockQuants(
            organizationId, productId, warehouseId, locationId, paginationRequest));
  }

  @GetMapping("/optimal-locations")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('quants:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<StockQuantResponse>> getOptimalLocations(
      @PathVariable UUID organizationId,
      @RequestParam UUID productId,
      @RequestParam BigDecimal quantity,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(
        pickingOptimalService.suggestLocations(organizationId, productId, quantity));
  }
}
