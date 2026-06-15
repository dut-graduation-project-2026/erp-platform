package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateSaleTeamRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateArchiveStatusRequest;
import com.dut.erp.dto.request.UpdateSaleTeamRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleTeamBaseResponse;
import com.dut.erp.dto.response.SaleTeamResponse;
import com.dut.erp.dto.response.UserBaseResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.SaleTeamService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for managing sale teams and their member assignments within an organization. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/sale-teams")
public class SaleTeamController {

  private final SaleTeamService saleTeamService;

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_teams:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleTeamResponse> createSaleTeam(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateSaleTeamRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(saleTeamService.createSaleTeam(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_teams:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleTeamResponse> updateSaleTeam(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateSaleTeamRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleTeamService.updateSaleTeam(organizationId, id, request));
  }

  @PatchMapping("/{id}/archive-status")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_teams:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleTeamResponse> updateSaleTeamArchiveStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateArchiveStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        saleTeamService.updateSaleTeamArchiveStatus(organizationId, id, request.isArchived()));
  }

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_teams:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<SaleTeamBaseResponse>> getSaleTeams(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "false") Boolean isArchived,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        saleTeamService.getSaleTeamsWithFilterByOrganizationId(
            organizationId, search, isArchived, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_teams:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<SaleTeamResponse> getSaleTeamById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleTeamService.getSaleTeamById(organizationId, id));
  }

  @GetMapping("/{id}/users")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_teams:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<List<UserBaseResponse>> getSaleTeamUsers(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(saleTeamService.getSaleTeamUsers(organizationId, id));
  }

  @GetMapping("/me")
  @PreAuthorize("@securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)")
  public ResponseEntity<List<SaleTeamResponse>> getMySaleTeams(
      @PathVariable UUID organizationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        saleTeamService.getMySaleTeamsByOrganizationId(organizationId, userDetails.getId()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('sale_teams:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteSaleTeam(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    saleTeamService.deleteSaleTeam(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
