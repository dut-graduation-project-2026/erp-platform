package com.dut.erp.dto.request;

import com.dut.erp.enums.AppointmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateCrmAppointmentRequest(
    @NotNull(message = "Lead ID is required") UUID leadId,
    @NotNull(message = "Appointment type is required") AppointmentType type,
    @NotBlank(message = "Title cannot be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
    String description,
    @NotNull(message = "Start time is required") Instant startTime,
    @NotNull(message = "End time is required") Instant endTime,
    @Size(max = 255) String location) {}
