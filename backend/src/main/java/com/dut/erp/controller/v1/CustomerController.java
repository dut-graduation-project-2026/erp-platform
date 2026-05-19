package com.dut.erp.controller.v1;

import com.dut.erp.dto.request.CreateCustomerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateCustomerRequest;
import com.dut.erp.dto.response.CustomerBaseResponse;
import com.dut.erp.dto.response.CustomerResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.security.CustomUserDetails;
import com.dut.erp.service.CustomerService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller handling customer management.
 * Provides CRUD endpoints for customers and their contacts within a specific organization.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}/customers")
public class CustomerController {

  private final CustomerService customerService;

  /**
   * Retrieves a paginated list of customers belonging to the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param query optional search keyword filtering by code, name, phone, or email
   * @param paginationRequest the pagination parameters (page and limit)
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing a paged response of CustomerBaseResponse objects
   */
  @GetMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('customers:read', #organizationId, #userDetails)
      """)
  public ResponseEntity<PagedEntityResponse<CustomerBaseResponse>> getCustomers(
      @PathVariable UUID organizationId,
      @RequestParam(required = false) String query,
      @Valid @ModelAttribute PaginationRequest paginationRequest,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        customerService.getCustomers(organizationId, query, paginationRequest));
  }

  /**
   * Retrieves details of a specific customer by ID within the specified organization.
   *
   * @param id the UUID of the customer
   * @param organizationId the UUID of the organization
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the CustomerResponse object
   */
  @GetMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('customers:select', #organizationId, #userDetails)
      """)
  public ResponseEntity<CustomerResponse> getCustomerById(
      @PathVariable UUID id,
      @PathVariable UUID organizationId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(customerService.getCustomerById(id, organizationId));
  }

  /**
   * Creates a new customer within the specified organization.
   *
   * @param organizationId the UUID of the organization
   * @param request the create customer request
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the created CustomerResponse object
   */
  @PostMapping
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('customers:create', #organizationId, #userDetails)
      """)
  public ResponseEntity<CustomerResponse> createCustomer(
      @PathVariable UUID organizationId,
      @Valid @RequestBody CreateCustomerRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(customerService.createCustomer(organizationId, request));
  }

  /**
   * Updates a specific customer in the organization.
   *
   * @param organizationId the UUID of the organization
   * @param id the UUID of the customer to update
   * @param request the update customer request
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity containing the updated CustomerResponse object
   */
  @PutMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('customers:modify', #organizationId, #userDetails)
      """)
  public ResponseEntity<CustomerResponse> updateCustomer(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @Valid @RequestBody UpdateCustomerRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(customerService.updateCustomer(id, organizationId, request));
  }

  /**
   * Deletes a specific customer from the organization.
   *
   * @param organizationId the UUID of the organization
   * @param id the UUID of the customer to delete
   * @param userDetails the authenticated user's details
   * @return a ResponseEntity with no content status (204)
   */
  @DeleteMapping("/{id}")
  @PreAuthorize(
      """
        @securityAuthService.hasOrganizationAccess(#organizationId, #userDetails)
        and
        @securityAuthService.hasPermission('customers:delete', #organizationId, #userDetails)
      """)
  public ResponseEntity<Void> deleteCustomer(
      @PathVariable UUID organizationId,
      @PathVariable UUID id,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    customerService.deleteCustomer(id, organizationId);
    return ResponseEntity.noContent().build();
  }
}
