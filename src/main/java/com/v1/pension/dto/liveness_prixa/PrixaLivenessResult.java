package com.v1.pension.dto.liveness_prixa;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrixaLivenessResult {
  private List<PrixaGestureDetectionLog> gestureDetectionLog;
  private List<PrixaFaceDetectionLog> faceDetectionLog;

  private String nationality;
  private String completed_at;
  private int bypassed;
  private int document_uplaod_retries;
  private String session;
  private boolean faceDetectionSuccess;
  private String document_number;
  private boolean gestureSuccess;
  private String gender;
  private boolean documentDetectionSuccess;
  private int in_progress;
  private int age;
  private String expires_at;
  private String userPhoto;
  private List<String> gesture_challenge;
  private double percentage_match;
  private int face_detection_retries;
  private int allow_force_next;
  private String started_at;
  private int gesture_verification_retries;
  private String label;
  private String secondary_label;
  private String identifier;
  private String jwt;

  @JsonProperty("is_verified")
  private boolean verified;

  @JsonProperty("forced_next")
  private boolean forcedNext;

  private String signature;
}
