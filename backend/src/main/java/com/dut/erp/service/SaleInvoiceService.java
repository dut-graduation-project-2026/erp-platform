package com.dut.erp.service;

import com.dut.erp.dto.request.CreateSaleInvoiceRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.RegisterPaymentRequest;
import com.dut.erp.dto.response.PagedEntityResponse;
import com.dut.erp.dto.response.SaleInvoiceBaseResponse;
import com.dut.erp.dto.response.SaleInvoiceResponse;
import java.util.UUID;

public interface SaleInvoiceService {

  PagedEntityResponse<SaleInvoiceBaseResponse> getInvoicesByOrganizationId(
      UUID organizationId, PaginationRequest paginationRequest);

  SaleInvoiceResponse getInvoiceById(UUID organizationId, UUID invoiceId);

  SaleInvoiceResponse createInvoice(UUID organizationId, CreateSaleInvoiceRequest request);

  SaleInvoiceResponse postInvoice(UUID organizationId, UUID invoiceId);

  SaleInvoiceResponse registerPayment(
      UUID organizationId, UUID invoiceId, RegisterPaymentRequest request);

  void cancelInvoice(UUID organizationId, UUID invoiceId);
}
