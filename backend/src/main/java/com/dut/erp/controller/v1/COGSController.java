package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.StockValuationResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.StockValuationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}")
public class COGSController {

  private final StockValuationService stockValuationService;

  @GetMapping("/orders/{orderId}/cogs")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('orders:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<StockValuationResponse>> getOrderCOGS(
      @PathVariable UUID organizationId,
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(stockValuationService.getValuationsByOrderId(organizationId, orderId));
  }
}
