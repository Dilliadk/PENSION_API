package com.v1.pension.service.liveness;

import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.CreateFaceLivenessSessionRequest;
import software.amazon.awssdk.services.rekognition.model.GetFaceLivenessSessionResultsRequest;
import software.amazon.awssdk.services.rekognition.model.GetFaceLivenessSessionResultsResponse;
import software.amazon.awssdk.services.rekognition.model.LivenessSessionStatus;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FaceLivenessService {
  private final RekognitionClient rekognitionClient;

  @Autowired
  public FaceLivenessService(RekognitionClient rekognitionClient) {
    this.rekognitionClient = rekognitionClient;
  }

  public String createLivenessSession() {
    var request = CreateFaceLivenessSessionRequest.builder().build();
    var response = rekognitionClient.createFaceLivenessSession(request);
    return response.sessionId();
  }

  /**
   * Retrieves the results of a Face Liveness session
   *
   * @param sessionId The session ID returned from createLivenessSession
   * @return GetFaceLivenessSessionResultsResponse containing confidence score and reference image
   */
  public GetFaceLivenessSessionResultsResponse getLivenessSessionResults(String sessionId) {
    try {
      var request = GetFaceLivenessSessionResultsRequest.builder().sessionId(sessionId).build();
      return rekognitionClient.getFaceLivenessSessionResults(request);
    } catch (RekognitionException e) {
      throw new RuntimeException("Failed to get face liveness session result.");
    }
  }

  public boolean isSessionLive(GetFaceLivenessSessionResultsResponse results, float confidenceThreshold) {
    if (results.status() != LivenessSessionStatus.SUCCEEDED)
      return false;

    // Check confidence score
    Float confidence = results.confidence();
    if (confidence == null)
      return false;

    return confidence >= confidenceThreshold;
  }
}
