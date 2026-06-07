package com.dut.erp.dto.response;

import com.dut.erp.enums.PartnerType;
import java.util.UUID;

public record PartnerBaseResponse(
    UUID id,
    String name,
    String taxCode,
    String email,
    String phone,
    Boolean isArchived,
    PartnerType partnerType) {}
