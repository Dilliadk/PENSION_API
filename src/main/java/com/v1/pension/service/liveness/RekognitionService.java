package com.v1.pension.service.liveness;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.util.Base64;
import java.util.List;

@Service
public class RekognitionService {

  private static final Logger logger = LoggerFactory.getLogger(RekognitionService.class);

  private final RekognitionClient rekognitionClient;

  @Autowired
  public RekognitionService(RekognitionClient rekognitionClient) {
    this.rekognitionClient = rekognitionClient;
  }

  public CompareFacesResponse compareFaces(byte[] sourceImageBytes, byte[] targetImageBytes) {
    try {
      Image sourceImage = Image.builder()
          .bytes(SdkBytes.fromByteArray(sourceImageBytes))
          .build();

      Image targetImage = Image.builder()
          .bytes(SdkBytes.fromByteArray(targetImageBytes))
          .build();

      CompareFacesRequest request = CompareFacesRequest.builder()
          .sourceImage(sourceImage)
          .targetImage(targetImage)
          .similarityThreshold(80F)
          .build();

      return rekognitionClient.compareFaces(request);
    } catch (RekognitionException e) {
      throw new RuntimeException("Error comparing faces: " + e.getMessage(), e);
    }
  }

  public DetectFacesResponse detectFaces(byte[] imageBytes) {
    try {
      Image image = Image.builder()
          .bytes(SdkBytes.fromByteArray(imageBytes))
          .build();

      DetectFacesRequest request = DetectFacesRequest.builder()
          .image(image)
          .attributes(Attribute.ALL)
          .build();

      return rekognitionClient.detectFaces(request);
    } catch (RekognitionException e) {
      throw new RuntimeException("Error detecting faces: " + e.getMessage(), e);
    }
  }

  public boolean isLivePerson(byte[] imageBytes) {
    try {
      DetectFacesResponse response = detectFaces(imageBytes);
      List<FaceDetail> faceDetails = response.faceDetails();

      logger.info("=== LIVENESS DETECTION DEBUG ===");

      if (faceDetails.isEmpty()) {
        logger.warn("No faces detected in image");
        return false;
      }

      FaceDetail face = faceDetails.get(0);

      // Extract all attributes
      boolean eyesOpen = face.eyesOpen() != null && face.eyesOpen().value();
      Float eyesOpenConfidence = face.eyesOpen() != null ? face.eyesOpen().confidence() : 0f;

      boolean mouthOpen = face.mouthOpen() != null && face.mouthOpen().value();
      Float mouthOpenConfidence = face.mouthOpen() != null ? face.mouthOpen().confidence() : 0f;

      boolean hasEyeglasses = face.eyeglasses() != null && face.eyeglasses().value();
      Float eyeglassesConfidence = face.eyeglasses() != null ? face.eyeglasses().confidence() : 0f;

      boolean hasSunglasses = face.sunglasses() != null && face.sunglasses().value();
      Float sunglassesConfidence = face.sunglasses() != null ? face.sunglasses().confidence() : 0f;

      // Quality metrics
      float brightness = face.quality() != null ? face.quality().brightness() : 0;
      float sharpness = face.quality() != null ? face.quality().sharpness() : 0;

      // Pose information
      Float pitch = face.pose() != null ? face.pose().pitch() : null;
      Float roll = face.pose() != null ? face.pose().roll() : null;
      Float yaw = face.pose() != null ? face.pose().yaw() : null;

      // Log all metrics
      logger.info("Eyes Open: {} (confidence: {}%)", eyesOpen, String.format("%.2f", eyesOpenConfidence));
      logger.info("Mouth Open: {} (confidence: {}%)", mouthOpen, String.format("%.2f", mouthOpenConfidence));
      logger.info("Eyeglasses: {} (confidence: {}%)", hasEyeglasses, String.format("%.2f", eyeglassesConfidence));
      logger.info("Sunglasses: {} (confidence: {}%)", hasSunglasses, String.format("%.2f", sunglassesConfidence));
      logger.info("Brightness: {}", String.format("%.2f", brightness));
      logger.info("Sharpness: {}", String.format("%.2f", sharpness));
      logger.info("Pose - Pitch: {}, Roll: {}, Yaw: {}",
          String.format("%.2f", pitch),
          String.format("%.2f", roll),
          String.format("%.2f", yaw));

      // Improved liveness checks with more lenient thresholds

      // 1. Eyes must be detected as open
      boolean eyesCheck = eyesOpen && eyesOpenConfidence > 50;
      logger.info("Eyes Check: {} (eyesOpen={}, confidence={}%)", eyesCheck, eyesOpen, String.format("%.2f", eyesOpenConfidence));

      // 2. Brightness check - more lenient for indoor lighting (LED lights)
      // Adjusted range: 20-95 (was 30-90)
      boolean brightnessCheck = brightness >= 20 && brightness <= 95;
      logger.info("Brightness Check: {} (value={}, range: 20-95)", brightnessCheck, String.format("%.2f", brightness));

      // 3. Sharpness check - more lenient
      // Adjusted threshold: 15 (was 20)
      boolean sharpnessCheck = sharpness >= 15;
      logger.info("Sharpness Check: {} (value={}, threshold: >=15)", sharpnessCheck, String.format("%.2f", sharpness));

      // 4. No sunglasses (high confidence sunglasses = likely spoofing)
      boolean sunglassesCheck = !hasSunglasses || sunglassesConfidence < 80;
      logger.info("Sunglasses Check: {} (hasSunglasses={}, confidence={}%)", sunglassesCheck, hasSunglasses, String.format("%.2f", sunglassesConfidence));

      // 5. Face pose check - face should be relatively frontal
      // Allow more flexibility in pose (-30 to +30 degrees for pitch and yaw)
      boolean poseCheck = true;
      if (pitch != null && yaw != null) {
        poseCheck = Math.abs(pitch) < 30 && Math.abs(yaw) < 30;
        logger.info("Pose Check: {} (pitch={}, yaw={}, threshold: <30 degrees)", poseCheck, String.format("%.2f", pitch), String.format("%.2f", yaw));
      }

      // Overall quality check - more lenient
      boolean qualityCheck = brightnessCheck && sharpnessCheck;
      logger.info("Quality Check: {} (brightness={}, sharpness={})", qualityCheck, brightnessCheck, sharpnessCheck);

      // Final liveness determination
      boolean isLive = eyesCheck && qualityCheck && sunglassesCheck && poseCheck;

      logger.info("=== FINAL LIVENESS RESULT: {} ===", isLive);
      logger.info("Eyes: {}, Quality: {}, Sunglasses: {}, Pose: {}", eyesCheck, qualityCheck, sunglassesCheck, poseCheck);

      if (!isLive) {
        logger.warn("Liveness check FAILED:");
        if (!eyesCheck) logger.warn("  - Eyes check failed");
        if (!qualityCheck) logger.warn("  - Quality check failed (brightness or sharpness)");
        if (!sunglassesCheck) logger.warn("  - Sunglasses detected");
        if (!poseCheck) logger.warn("  - Face pose not frontal enough");
      }

      return isLive;

    } catch (RekognitionException e) {
      logger.error("Error checking liveness: {}", e.getMessage(), e);
      throw new RuntimeException("Error checking liveness: " + e.getMessage(), e);
    }
  }

  public byte[] base64ToBytes(String base64String) {
    // Remove data URL prefix if present
    if (base64String.contains(",")) {
      base64String = base64String.split(",")[1];
    }
    return Base64.getDecoder().decode(base64String);
  }
}
