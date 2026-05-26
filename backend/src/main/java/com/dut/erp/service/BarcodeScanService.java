package com.dut.erp.service;

import com.dut.erp.dto.response.BarcodeScanResponse;
import java.util.UUID;

public interface BarcodeScanService {

  BarcodeScanResponse scanBarcode(UUID organizationId, String barcode);
}
