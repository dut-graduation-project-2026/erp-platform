package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.ProductCategoryRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductCategoryBaseResponse;
import com.dut.erp.dto.response.ProductCategoryResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.ProductCategoryService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/product-categories")
public class ProductCategoryController {

  private final ProductCategoryService productCategoryService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('product_categories:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<ProductCategoryBaseResponse>> getCategories(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        productCategoryService.getCategoriesByOrganizationId(
            organizationId, search, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('product_categories:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductCategoryResponse> getCategoryById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(productCategoryService.getCategoryById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('product_categories:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductCategoryResponse> createCategory(
      @PathVariable UUID organizationId,
      @Valid @RequestBody ProductCategoryRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(productCategoryService.createCategory(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('product_categories:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductCategoryResponse> updateCategory(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody ProductCategoryRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(productCategoryService.updateCategory(organizationId, id, request));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('product_categories:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteCategory(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    productCategoryService.deleteCategory(organizationId, id);
    return ResponseEntity.noContent().build();
  }
}
