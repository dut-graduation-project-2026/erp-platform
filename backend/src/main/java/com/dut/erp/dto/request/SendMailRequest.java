package com.dut.erp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendMailRequest(
    @NotBlank(message = "To email is required") @Email(message = "Email should be valid") String to,
    @NotBlank(message = "Subject is required") String subject,
    @NotBlank(message = "Content is required") String content,
    boolean isHtml) {}
