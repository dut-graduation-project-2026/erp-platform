package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ForecastPoint(
    String date,
    @JsonProperty("historical_revenue") BigDecimal historicalRevenue,
    @JsonProperty("predicted_revenue") BigDecimal predictedRevenue
) {}
