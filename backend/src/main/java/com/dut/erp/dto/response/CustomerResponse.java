package com.dut.erp.dto.response;

import com.dut.erp.enums.CustomerStatus;
import com.dut.erp.enums.CustomerType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String code,
    String name,
    CustomerType type,
    String email,
    String phone,
    String address,
    String taxCode,
    CustomerStatus status,
    List<CustomerContactResponse> contacts,
    Instant createdAt,
    Instant updatedAt,
    UserBaseResponse createdBy,
    UserBaseResponse updatedBy
) {}
