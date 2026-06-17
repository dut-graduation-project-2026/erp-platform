package com.dut.erp.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cloudinary")
public record CloudinaryProperties(
    @NotBlank(message = "Cloudinary Cloud Name is required")
    String cloudName,
    @NotBlank(message = "Cloudinary API Key is required")
    String apiKey,
    @NotBlank(message = "Cloudinary API Secret is required")
    String apiSecret
) {}
