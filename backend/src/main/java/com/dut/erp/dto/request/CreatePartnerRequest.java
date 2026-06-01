package com.dut.erp.dto.request;

import com.dut.erp.annotation.ValueOfEnum;
import com.dut.erp.enums.PartnerType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreatePartnerRequest(
    @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name cannot exceed 255 characters")
        String name,
    @Size(max = 50, message = "Tax code cannot exceed 50 characters") String taxCode,
    @Size(max = 255, message = "Email cannot exceed 255 characters")
        @Email(message = "Email must be a valid email address")
        String email,
    @Size(max = 50, message = "Phone cannot exceed 50 characters") String phone,
    @Size(max = 255, message = "Address cannot exceed 255 characters") String address,
    @Size(max = 255, message = "Job position cannot exceed 255 characters") String jobPosition,
    @Size(max = 2000, message = "Notes cannot exceed 2000 characters") String notes,
    @Pattern(regexp = "^\\S+$", message = "Partner type must not contain whitespace")
        @NotNull(message = "Partner type cannot be null")
        @ValueOfEnum(
            enumClass = PartnerType.class,
            message = "Partner type must be one of: {enumValues}")
        String partnerType,
    List<PartnerContactRequest> contacts) {}
