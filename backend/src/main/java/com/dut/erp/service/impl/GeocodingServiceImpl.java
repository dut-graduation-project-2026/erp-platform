package com.dut.erp.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.dut.erp.config.properties.GeoapifyProperties;
import com.dut.erp.dto.response.GeocodingSearchResult;
import com.dut.erp.service.GeocodingService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class GeocodingServiceImpl implements GeocodingService {

  private final RestClient restClient;
  private final GeoapifyProperties geoapifyProperties;
  private final Map<String, List<GeocodingSearchResult>> cache = new ConcurrentHashMap<>();

  public GeocodingServiceImpl(
      @Qualifier("geoapifyRestClient") RestClient restClient,
      GeoapifyProperties geoapifyProperties) {
    this.restClient = restClient;
    this.geoapifyProperties = geoapifyProperties;
  }

  // Geoapify API models
  private record GeoapifyResponse(
      List<GeoapifyFeature> features
  ) {}

  private record GeoapifyFeature(
      GeoapifyFeatureProperties properties
  ) {}

  private record GeoapifyFeatureProperties(
      @JsonProperty("place_id") String placeId,
      String formatted,
      @JsonProperty("address_line1") String addressLine1,
      @JsonProperty("address_line2") String addressLine2,
      String name,
      Double lat,
      Double lon
  ) {}

  @Override
  public List<GeocodingSearchResult> autocomplete(String text, String focus) {
    if (text == null || text.isBlank()) {
      return List.of();
    }

    String cacheKey = "auto:" + text.trim().toLowerCase();
    if (cache.containsKey(cacheKey)) {
      log.debug("Returning cached autocomplete result for: {}", text);
      return cache.get(cacheKey);
    }

    log.info("Querying Geoapify autocomplete for text: {}", text);
    try {
      GeoapifyResponse response = restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/v1/geocode/autocomplete")
                      .queryParam("text", text)
                      .queryParam("apiKey", geoapifyProperties.apiKey())
                      .queryParam("lang", "en")
                      .queryParam("limit", 10)
                      .build())
          .retrieve()
          .body(GeoapifyResponse.class);

      List<GeocodingSearchResult> mapped = List.of();
      if (response != null && response.features() != null) {
        mapped = response.features().stream().map(this::mapToGeocodingSearchResult).toList();
      }

      cache.put(cacheKey, mapped);
      return mapped;
    } catch (Exception e) {
      log.error("Failed to query Geoapify autocomplete", e);
      return List.of();
    }
  }

  @Override
  public List<GeocodingSearchResult> search(String text, String focus) {
    if (text == null || text.isBlank()) {
      return List.of();
    }

    String cacheKey = "search:" + text.trim().toLowerCase();
    if (cache.containsKey(cacheKey)) {
      log.debug("Returning cached search result for: {}", text);
      return cache.get(cacheKey);
    }

    log.info("Querying Geoapify search for text: {}", text);
    try {
      GeoapifyResponse response = restClient
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/v1/geocode/search")
                      .queryParam("text", text)
                      .queryParam("apiKey", geoapifyProperties.apiKey())
                      .queryParam("lang", "en")
                      .queryParam("limit", 10)
                      .build())
          .retrieve()
          .body(GeoapifyResponse.class);

      List<GeocodingSearchResult> mapped = List.of();
      if (response != null && response.features() != null) {
        mapped = response.features().stream().map(this::mapToGeocodingSearchResult).toList();
      }

      cache.put(cacheKey, mapped);
      return mapped;
    } catch (Exception e) {
      log.error("Failed to query Geoapify search", e);
      return List.of();
    }
  }

  private GeocodingSearchResult mapToGeocodingSearchResult(GeoapifyFeature feature) {
    GeoapifyFeatureProperties props = feature.properties();
    String display = props.formatted() != null ? props.formatted() : "";

    // Choose fallback name and address fields
    String name = props.name() != null ? props.name() : props.addressLine1();
    if (name == null || name.isBlank()) {
      name = display;
    }

    String address = props.addressLine2();
    if (address == null || address.isBlank()) {
      address = display;
    }

    return new GeocodingSearchResult(
        props.placeId() != null ? props.placeId() : "",
        display,
        address,
        name,
        props.lat(),
        props.lon()
    );
  }
}
