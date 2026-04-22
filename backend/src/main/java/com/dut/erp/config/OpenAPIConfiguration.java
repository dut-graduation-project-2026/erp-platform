package com.dut.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfiguration {
  @Bean
  public OpenAPI erpOpenAPI() {
    Info info =
        new Info()
            .title("ERP Platform API")
            .version("0.0.1")
            .description("API documentation for the ERP Platform backend");
    return new OpenAPI().info(info);
  }
}
