package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateInventoryDocumentRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.response.InventoryDocumentBaseResponse;
import com.dut.erp.dto.response.InventoryDocumentResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.InventoryDocumentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/warehouses/{warehouseId}")
public class InventoryDocumentController {

  private final InventoryDocumentService inventoryDocumentService;

  @PostMapping("/orders/{orderId}/claim")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryDocumentResponse> createIssueDocumentFromOrder(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID orderId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDocumentService.createIssueDocumentFromOrder(organizationId, warehouseId, orderId));
  }

  @GetMapping("/documents")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<InventoryDocumentBaseResponse>> getDocuments(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String type,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDocumentService.getDocuments(organizationId, warehouseId, search, status, type, paginationRequest));
  }

  @GetMapping("/documents/{documentId}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryDocumentResponse> getDocumentById(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID documentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDocumentService.getDocumentById(organizationId, warehouseId, documentId));
  }

  @PostMapping("/documents/{documentId}/confirm")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryDocumentResponse> confirmDocument(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID documentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDocumentService.confirmDocument(organizationId, warehouseId, documentId));
  }

  @PostMapping("/documents/{documentId}/complete")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryDocumentResponse> completeDocument(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID documentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDocumentService.completeDocument(organizationId, warehouseId, documentId));
  }

  @PostMapping("/documents/{documentId}/sent")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryDocumentResponse> sentDocument(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID documentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDocumentService.sentDocument(organizationId, warehouseId, documentId));
  }

  @PostMapping("/documents/{documentId}/cancel")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryDocumentResponse> cancelDocument(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @PathVariable UUID documentId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(inventoryDocumentService.cancelDocument(organizationId, warehouseId, documentId));
  }

  @PostMapping("/documents")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('warehouses:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<InventoryDocumentResponse> createDocument(
      @PathVariable UUID organizationId,
      @PathVariable UUID warehouseId,
      @Valid @RequestBody CreateInventoryDocumentRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(inventoryDocumentService.createDocument(organizationId, warehouseId, request));
  }
}
