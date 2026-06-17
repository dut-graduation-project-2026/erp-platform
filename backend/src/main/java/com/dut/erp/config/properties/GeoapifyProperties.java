package com.dut.erp.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.geoapify")
public record GeoapifyProperties(
    @NotBlank(message = "Geoapify API Key is required")
    String apiKey
) {}
