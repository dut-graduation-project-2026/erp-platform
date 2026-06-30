package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiSalesForecastResponse(
    String summary,
    @JsonProperty("forecast_30d_total_revenue") BigDecimal forecast30dTotalRevenue,
    @JsonProperty("forecast_points") List<ForecastPoint> forecastPoints,
    List<String> insights
) {}
