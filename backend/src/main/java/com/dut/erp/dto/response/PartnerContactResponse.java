package com.dut.erp.dto.response;

import java.util.UUID;

public record PartnerContactResponse(
    UUID id,
    String name,
    String email,
    String phone,
    String jobPosition,
    String notes,
    UUID partnerId) {}
