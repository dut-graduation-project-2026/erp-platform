package com.dut.erp.dto.response;

public record GeocodingSearchResult(
    String refId,
    String display,
    String address,
    String name,
    Double lat,
    Double lng
) {}
