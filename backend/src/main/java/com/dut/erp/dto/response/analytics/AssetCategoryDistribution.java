package com.dut.erp.dto.response.analytics;

import java.math.BigDecimal;
import java.util.UUID;

public record AssetCategoryDistribution(
    UUID categoryId,
    String categoryName,
    BigDecimal totalAssetValue,
    double percentage,
    BigDecimal totalQuantity,
    long productCount
) {}
