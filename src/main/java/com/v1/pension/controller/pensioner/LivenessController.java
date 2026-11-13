package com.v1.pension.controller.pensioner;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.liveness.LivenessVerificationRequest;
import com.v1.pension.dto.liveness.LivenessVerificationResponse;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.service.liveness.FaceLivenessService;
import com.v1.pension.service.liveness.LivenessVerificationService;

@RestController
@RequestMapping("/api/liveness")
@RequiredArgsConstructor
public class LivenessController {
  private final LivenessVerificationService livenessVerificationService;
  private final FaceLivenessService faceLivenessService;

  @PostMapping("/verify")
  public ResponseEntity<LivenessVerificationResponse> verifyLiveness(
      @RequestBody LivenessVerificationRequest request) {
    try {
      var response = livenessVerificationService.verifyLiveness(request.getVideoFrameBase64());

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new LivenessVerificationResponse(
              false,
              false,
              0.0f,
              "Error during verification: " + e.getMessage(),
              false));
    }
  }

  @PostMapping("/create-liveness-session")
  public ResponseEntity<Map<String, String>> createLivenessSession() {
    try {
      String sessionId = faceLivenessService.createLivenessSession();
      Map<String, String> response = new HashMap<>();
      response.put("sessionId", sessionId);
      response.put("message", "Face Liveness session created successfully");
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, String> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @GetMapping("/liveness-results/{sessionId}")
  public ResponseEntity<Map<String, Object>> getLivenessResults(@PathVariable String sessionId) {
    try {
      software.amazon.awssdk.services.rekognition.model.GetFaceLivenessSessionResultsResponse results = faceLivenessService
          .getLivenessSessionResults(sessionId);

      Map<String, Object> response = new HashMap<>();
      response.put("sessionId", sessionId);
      response.put("status", results.status().toString());
      response.put("confidence", results.confidence());

      if (results.referenceImage() != null && results.referenceImage().bytes() != null) {
        response.put("hasReferenceImage", true);
        response.put("referenceImageSize", results.referenceImage().bytes().asByteArray().length);
      } else {
        response.put("hasReferenceImage", false);
      }

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      Map<String, Object> errorResponse = new HashMap<>();
      errorResponse.put("error", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @PostMapping("/verify-with-liveness")
  public ResponseEntity<LivenessVerificationResponse> verifyWithLiveness(@RequestBody Map<String, String> request) {
    var livenessSessionId = request.get("livenessSessionId");
    var dateNep = request.get("dateNep");

    if (livenessSessionId == null)
      throw new BadRequestException("Liveness Session Id is required");

    var response = livenessVerificationService.verifyWithFaceLiveness(livenessSessionId, dateNep);
    return ResponseEntity.ok(response);
  }
}
