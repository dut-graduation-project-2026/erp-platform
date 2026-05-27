package com.dut.erp.dto.response;

import com.dut.erp.enums.PartnerStatus;
import com.dut.erp.enums.PartnerType;
import java.util.UUID;

public record SalePartnerBaseResponse(
    UUID id,
    String code,
    String name,
    PartnerType partnerType,
    PartnerStatus status) {}
