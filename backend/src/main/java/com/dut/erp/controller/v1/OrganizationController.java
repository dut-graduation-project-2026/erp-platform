package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
  private final OrganizationService organizationService;

  @GetMapping("/me")
  public ResponseEntity<List<OrganizationResponse>> getOrganizationOfCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<OrganizationResponse> organizationResponses =
        organizationService.getOrganizationByUserId(userDetails.getId());
    return ResponseEntity.ok(organizationResponses);
  }
}
