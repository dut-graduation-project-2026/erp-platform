package com.dut.erp.dto.response;

import java.util.UUID;

public record SalesTeamResponse(UUID id, String name, UserBaseResponse leader) {}
