package com.dut.erp.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.dut.erp.service.CloudinaryService;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

  private final Cloudinary cloudinary;

  @Override
  public String uploadImage(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return null;
    }
    try {
      log.info("Uploading file '{}' to Cloudinary", file.getOriginalFilename());
      Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("resource_type", "auto"));
      String secureUrl = (String) uploadResult.get("secure_url");
      log.info("Successfully uploaded file. Secure URL: {}", secureUrl);
      return secureUrl;
    } catch (IOException e) {
      log.error("Failed to upload file to Cloudinary", e);
      throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
    }
  }
}
