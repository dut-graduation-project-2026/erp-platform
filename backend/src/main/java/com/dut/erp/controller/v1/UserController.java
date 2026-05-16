package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateUserRequest;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

  /**
   * Updates the user information.
   *
   * <p>The user is resolved from the security context. The authenticated user can only update
   * their own profile information.
   *
   * @param userId the UUID of the user to update
   * @param request the update request containing user details
   * @param userDetails the authenticated user's details (injected from security context)
   * @return a ResponseEntity containing the updated UserBaseResponse object
   * @throws AccessDeniedException if the authenticated user is not the target user
   * @throws ResourceNotFoundException if the user does not exist
   */
  @PutMapping("/{userId}")
  @PreAuthorize("#userId == #userDetails.id")
  public ResponseEntity<UserBaseResponse> updateUser(
      @PathVariable UUID userId,
      @Valid @RequestBody UpdateUserRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    UserBaseResponse userResponse = userService.updateUser(userId, request);
    return ResponseEntity.ok(userResponse);
  }
}

