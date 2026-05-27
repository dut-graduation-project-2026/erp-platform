package com.dut.erp.dto.request;

import com.dut.erp.enums.PartnerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateSalePartnerRequest(
    @NotBlank(message = "Partner code cannot be blank")
        @Size(max = 50, message = "Partner code must not exceed 50 characters")
        String code,
    @NotBlank(message = "Partner name cannot be blank")
        @Size(max = 255, message = "Partner name must not exceed 255 characters")
        String name,
    @NotNull(message = "Partner type is required") PartnerType partnerType,
    @Size(max = 50) String taxCode,
    @Size(max = 255) String email,
    @Size(max = 50) String phone,
    @Size(max = 255) String address,
    List<PartnerContactRequest> contacts) {}
