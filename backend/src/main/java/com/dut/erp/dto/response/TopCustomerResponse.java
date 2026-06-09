package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record TopCustomerResponse(
    UUID partnerId,
    String partnerName,
    BigDecimal totalSpend,
    Long orderCount
) {}
