package com.dut.erp.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductBaseResponse(UUID id, String name, BigDecimal price, Boolean isArchived) {}
