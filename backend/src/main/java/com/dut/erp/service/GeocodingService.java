package com.dut.erp.service;

import com.dut.erp.dto.response.GeocodingSearchResult;
import java.util.List;

public interface GeocodingService {
  List<GeocodingSearchResult> autocomplete(String text, String focus);
  List<GeocodingSearchResult> search(String text, String focus);
}
