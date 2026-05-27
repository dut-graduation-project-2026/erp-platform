package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.UpdateOrganizationMemberRolesRequest;
import com.dut.erp.dto.response.OrganizationMemberResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.OrganizationMemberService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling organization members endpoints.
 */
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/members")
public class OrganizationMemberController {
  private final OrganizationMemberService organizationMemberService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('users:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<OrganizationMemberResponse>> getMembers(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String query,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        organizationMemberService.getMembers(organizationId, query, paginationRequest));
  }

  @GetMapping("/{userId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('users:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrganizationMemberResponse> getMemberById(
      @PathVariable UUID organizationId,
      @PathVariable UUID userId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(organizationMemberService.getMemberById(organizationId, userId));
  }

  @PutMapping("/{userId}/roles")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('users:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<OrganizationMemberResponse> updateMemberRoles(
      @PathVariable UUID organizationId,
      @PathVariable UUID userId,
      @RequestBody @Valid UpdateOrganizationMemberRolesRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        organizationMemberService.updateMemberRoles(organizationId, userId, request));
  }

  @DeleteMapping("/{userId}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('users:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> removeMember(
      @PathVariable UUID organizationId,
      @PathVariable UUID userId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    organizationMemberService.removeMember(organizationId, userId);
    return ResponseEntity.noContent().build();
  }
}
