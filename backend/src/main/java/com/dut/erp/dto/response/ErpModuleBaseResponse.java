package com.dut.erp.dto.response;

import java.util.UUID;

public record ErpModuleBaseResponse(UUID id, String name, String code, String description) {}
