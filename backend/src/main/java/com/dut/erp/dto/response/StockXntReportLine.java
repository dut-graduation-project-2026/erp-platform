package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record StockXntReportLine(
    UUID productId,
    String sku,
    String name,
    BigDecimal beginningQty,
    BigDecimal beginningValue,
    BigDecimal inboundQty,
    BigDecimal inboundValue,
    BigDecimal outboundQty,
    BigDecimal outboundValue,
    BigDecimal endingQty,
    BigDecimal endingValue
) {}
