package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateProductRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateProductRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.ProductBaseResponse;
import com.dut.erp.dto.response.ProductResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controller for managing products and services catalog within an organization. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/products")
public class ProductController {

  private final ProductService productService;

  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<ProductBaseResponse>> getProducts(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) @Size(max = 30) String search,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        productService.getProductsWithFilterByOrganizationId(
            organizationId, search, paginationRequest));
  }

  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('products:read', #organizationId, #userDetails)
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
      @Valid @RequestBody CreateProductRequest request,
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
      @Valid @RequestBody UpdateProductRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(productService.updateProduct(organizationId, id, request));
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
}
