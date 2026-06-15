package com.dut.erp.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.domains")
public record SystemDomainProperties(
    @NotBlank(message = "Frontend domain is required") String frontend,
    @NotBlank(message = "Backend domain is required") String backend,
    @NotBlank(message = "AI domain is required") String ai) {}
