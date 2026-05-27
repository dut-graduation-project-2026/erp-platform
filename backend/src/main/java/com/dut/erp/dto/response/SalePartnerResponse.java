package com.dut.erp.dto.response;

import com.dut.erp.enums.PartnerStatus;
import com.dut.erp.enums.PartnerType;
import java.util.List;
import java.util.UUID;

public record SalePartnerResponse(
    UUID id,
    String code,
    String name,
    PartnerType partnerType,
    String taxCode,
    String email,
    String phone,
    String address,
    PartnerStatus status,
    List<PartnerContactResponse> contacts,
    OrganizationBaseResponse organization) {}
