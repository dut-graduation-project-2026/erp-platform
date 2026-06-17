package com.dut.erp.dto.response;

import com.dut.erp.enums.PartnerType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PartnerBaseResponse(
    UUID id,
    String name,
    String taxCode,
    String email,
    String phone,
    String address,
    Boolean isArchived,
    PartnerType partnerType) {}
