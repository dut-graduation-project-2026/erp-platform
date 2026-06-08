package com.dut.erp.dto.response;

import com.dut.erp.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderBaseResponse(
    UUID id, String orderNumber, OrderStatus status, BigDecimal totalAmount) {}
