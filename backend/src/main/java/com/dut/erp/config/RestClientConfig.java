package com.dut.erp.config;

import com.dut.erp.config.properties.SystemDomainProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

  private final SystemDomainProperties systemDomainProperties;

  @Bean(name = "aiServiceRestClient")
  public RestClient aiServiceRestClient() {
    String aiBaseUrl = systemDomainProperties.ai();
    if (aiBaseUrl != null && !aiBaseUrl.startsWith("http://") && !aiBaseUrl.startsWith("https://")) {
      aiBaseUrl = "https://" + aiBaseUrl;
    }
    return RestClient.builder()
        .baseUrl(aiBaseUrl)
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("Accept", "application/json")
        .build();
  }

  @Bean(name = "geoapifyRestClient")
  public RestClient geoapifyRestClient() {
    return RestClient.builder()
        .baseUrl("https://api.geoapify.com")
        .defaultHeader("Accept", "application/json")
        .build();
  }
}
