package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateArchiveStatusRequest;
import com.dut.erp.dto.request.UpsertProductRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.ProductService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for managing products and services catalog within an organization. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/products")
public class ProductController {

  private final ProductService productService;
  private final com.dut.erp.service.CloudinaryService cloudinaryService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<ProductBaseResponse>> getProducts(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "false") Boolean isArchived,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        productService.getProductsWithFilterByOrganizationId(
            organizationId, search, isArchived, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductResponse> getProductById(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(productService.getProductById(organizationId, id));
  }

  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductResponse> createProduct(
      @PathVariable UUID organizationId,
      @Valid @RequestBody UpsertProductRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(productService.createProduct(organizationId, request));
  }

  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductResponse> updateProduct(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpsertProductRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(productService.updateProduct(organizationId, id, request));
  }

  @PatchMapping("/{id}/archive-status")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<ProductResponse> updateProductArchiveStatus(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateArchiveStatusRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        productService.updateProductArchiveStatus(organizationId, id, request.isArchived()));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteProduct(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    productService.deleteProduct(organizationId, id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/upload")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:write', #organizationId, #userDetails)
      """)
  public ResponseEntity<java.util.Map<String, String>> uploadProductImage(
      @PathVariable UUID organizationId,
      @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    String url = cloudinaryService.uploadImage(file);
    return ResponseEntity.ok(java.util.Map.of("url", url != null ? url : ""));
  }
}
