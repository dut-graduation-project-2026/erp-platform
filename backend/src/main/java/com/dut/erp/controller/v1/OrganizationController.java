package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.OrganizationResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
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

  /**
   * Retrieves all organizations that the current authenticated user belongs to.
   *
   * <p>The user is resolved from the security context. Returns an empty list if the user is not a
   * member of any organization.
   *
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing a list of OrganizationResponse objects
   */
  @Operation(
      summary = "Get organizations of current user",
      description =
          "Returns a list of organizations that the current authenticated user belongs to.")
  @GetMapping("/me")
  public ResponseEntity<List<OrganizationResponse>> getOrganizationsOfCurrentUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<OrganizationResponse> organizationResponses =
        organizationService.getOrganizationsByUserId(userDetails.getId());
    return ResponseEntity.ok(organizationResponses);
  }
}
