package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.UserService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {
  private final UserService userService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('organizations:manage', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<UserBaseResponse>> getUsersOfOrganization(
      @RequestParam UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    PagedEntityResponse<UserBaseResponse> response =
        userService.getUsersByOrganizationId(organizationId, paginationRequest);
    return ResponseEntity.ok(response);
  }
}
