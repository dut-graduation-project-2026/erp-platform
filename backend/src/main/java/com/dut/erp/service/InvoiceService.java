package com.dut.erp.service;

import com.dut.erp.dto.request.CreateInvoiceRequest;
import com.dut.erp.dto.request.PaginationRequest;
import com.dut.erp.dto.request.UpdateInvoiceStatusRequest;
import com.dut.erp.dto.request.RegisterPaymentRequest;
import com.dut.erp.dto.response.InvoiceBaseResponse;
import com.dut.erp.dto.response.InvoiceResponse;
import com.dut.erp.dto.response.PagedEntityResponse;
import java.util.UUID;

public interface InvoiceService {

  InvoiceResponse createInvoiceFromOrder(UUID organizationId, CreateInvoiceRequest request);

  InvoiceResponse updateInvoiceStatus(
      UUID organizationId, UUID id, UpdateInvoiceStatusRequest request);

  InvoiceResponse registerPayment(
      UUID organizationId, UUID id, RegisterPaymentRequest request);

  InvoiceResponse getInvoiceById(UUID organizationId, UUID id);

  InvoiceResponse getInvoiceByOrderId(UUID organizationId, UUID orderId);

  PagedEntityResponse<InvoiceBaseResponse> getInvoices(
      UUID organizationId, String search, String status, PaginationRequest paginationRequest);
}
