package com.dut.erp.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "spring.mail")
public record EmailProperties(
    @NotBlank(message = "SMTP host is required") String host,
    @NotBlank(message = "SMTP port is required") String port,
    @NotBlank(message = "SMTP username is required") String username,
    @NotBlank(message = "SMTP password is required") String password
) {}
