package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateStockLotRequest;
import com.dut.erp.dto.response.StockLotResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.StockLotService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}")
public class StockLotController {

  private final StockLotService stockLotService;

  @PostMapping("/lots")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('quants:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockLotResponse> createLot(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateStockLotRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(stockLotService.createLot(organizationId, request));
  }

  @GetMapping("/lots/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('quants:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<StockLotResponse> getLotById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockLotService.getLotById(organizationId, id));
  }

  @GetMapping("/products/{productId}/lots")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('quants:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<StockLotResponse>> getLotsByProductId(
      @PathVariable UUID organizationId,
      @PathVariable UUID productId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(stockLotService.getLotsByProductId(organizationId, productId));
  }
}
