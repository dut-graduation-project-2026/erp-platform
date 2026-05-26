package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateProductTemplateRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateProductTemplateRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductTemplateBaseResponse;
import com.dut.erp.dto.response.ProductTemplateResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.ProductTemplateService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/product-templates")
public class ProductTemplateController {

  private final ProductTemplateService productTemplateService;

  @GetMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<ProductTemplateBaseResponse>> getTemplates(
      @PathVariable UUID organizationId,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        productTemplateService.getTemplatesByOrganizationId(organizationId, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductTemplateResponse> getTemplateById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(productTemplateService.getTemplateById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductTemplateResponse> createTemplate(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateProductTemplateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(productTemplateService.createTemplate(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductTemplateResponse> updateTemplate(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateProductTemplateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(productTemplateService.updateTemplate(organizationId, id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("""
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteTemplate(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    productTemplateService.deleteTemplate(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
