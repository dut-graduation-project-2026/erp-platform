package com.dut.erp.service;

import com.dut.erp.dto.request.CreateCustomerRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateCustomerRequest;
import com.dut.erp.dto.response.CustomerBaseResponse;
import com.dut.erp.dto.response.CustomerResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import java.util.UUID;

public interface CustomerService {
  PagedEntityResponse<CustomerBaseResponse> getCustomers(
      UUID organizationId, String query, PaginationRequest paginationRequest);

  CustomerResponse getCustomerById(UUID id, UUID organizationId);

  CustomerResponse createCustomer(UUID organizationId, CreateCustomerRequest request);

  CustomerResponse updateCustomer(UUID id, UUID organizationId, UpdateCustomerRequest request);

  void deleteCustomer(UUID id, UUID organizationId);
}
