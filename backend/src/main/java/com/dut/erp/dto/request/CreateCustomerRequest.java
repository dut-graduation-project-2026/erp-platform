package com.dut.erp.dto.request;

import com.dut.erp.enums.CustomerStatus;
import com.dut.erp.enums.CustomerType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateCustomerRequest(
    @NotBlank(message = "Customer code is required")
    @Size(max = 50, message = "Customer code must not exceed 50 characters")
    String code,

    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Customer name must not exceed 200 characters")
    String name,

    @NotNull(message = "Customer type is required")
    CustomerType type,

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    @Size(max = 50, message = "Phone must not exceed 50 characters")
    String phone,

    @Size(max = 255, message = "Address must not exceed 255 characters")
    String address,

    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    String taxCode,

    @NotNull(message = "Customer status is required")
    CustomerStatus status,

    @Valid
    List<CreateCustomerContactRequest> contacts
) {}
