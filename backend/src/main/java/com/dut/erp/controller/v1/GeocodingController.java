package com.dut.erp.controller.v1;

import com.dut.erp.dto.response.GeocodingSearchResult;
import com.dut.erp.service.GeocodingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/geocoding")
public class GeocodingController {

  private final GeocodingService geocodingService;

  @GetMapping("/autocomplete")
  public ResponseEntity<List<GeocodingSearchResult>> autocomplete(
      @RequestParam String text,
      @RequestParam(required = false) String focus) {
    return ResponseEntity.ok(geocodingService.autocomplete(text, focus));
  }

  @GetMapping("/search")
  public ResponseEntity<List<GeocodingSearchResult>> search(
      @RequestParam String text,
      @RequestParam(required = false) String focus) {
    return ResponseEntity.ok(geocodingService.search(text, focus));
  }
}
