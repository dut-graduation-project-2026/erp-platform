package com.dut.erp.dto.response;

import com.dut.erp.enums.AppointmentStatus;
import com.dut.erp.enums.AppointmentType;
import java.time.Instant;
import java.util.UUID;

public record CrmAppointmentResponse(
    UUID id,
    UUID leadId,
    AppointmentType type,
    String title,
    String description,
    Instant startTime,
    Instant endTime,
    String location,
    AppointmentStatus status) {}
