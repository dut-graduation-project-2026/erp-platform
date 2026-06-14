package com.dut.erp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiInventoryAnalysisResponse(
    String summary,
    @JsonProperty("abc_xyz_matrix") List<AiProductAbcXyz> abcXyzMatrix,
    @JsonProperty("critical_stock_count") Integer criticalStockCount,
    List<String> recommendations
) {}
