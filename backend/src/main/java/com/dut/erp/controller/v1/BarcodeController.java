package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.BarcodeScanRequest;
import com.dut.erp.dto.response.BarcodeScanResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.BarcodeScanService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/barcode")
public class BarcodeController {

  private final BarcodeScanService barcodeScanService;

  @PostMapping("/scan")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('quants:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<BarcodeScanResponse> scanBarcode(
      @PathVariable UUID organizationId,
      @Valid @RequestBody BarcodeScanRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    return ResponseEntity.ok(barcodeScanService.scanBarcode(organizationId, request.barcode()));
  }
}
