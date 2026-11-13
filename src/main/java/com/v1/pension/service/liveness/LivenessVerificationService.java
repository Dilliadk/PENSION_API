package com.v1.pension.service.liveness;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.rekognition.model.CompareFacesMatch;
import software.amazon.awssdk.services.rekognition.model.CompareFacesResponse;

import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.v1.pension.dto.liveness.LivenessVerificationResponse;
import com.v1.pension.dto.nid.NinRequestDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.PensionerPresence;
import com.v1.pension.repository.pensioner.PensionPresenceRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.pensioner.PensionerVirtualPresenceRepository;
import com.v1.pension.service.other.ImageConversionService;
import com.v1.pension.service.other.NidService;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class LivenessVerificationService {

  private final RekognitionService rekognitionService;
  private final FaceLivenessService faceLivenessService;
  private final PensionerVirtualPresenceRepository virtualPresenceRepository;
  private final PensionPresenceRepository pensionPresenceRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final ImageConversionService imageConversionService;
  private final NidService nidService;

  public LivenessVerificationResponse verifyLiveness(String videoFrameBase64) {
    try {
      var virtualPresenceData = virtualPresenceRepository.findLatestBySubPNo(CurrentUser.getUserName())
          .orElseThrow(() -> new BadRequestException("Virtual Presence Registration Not Found"));

      // Convert video frame from base64
      byte[] videoFrameBytes = rekognitionService.base64ToBytes(videoFrameBase64);

      // Step 1: Check if the person in the video is live
      boolean isLive = rekognitionService.isLivePerson(videoFrameBytes);

      if (!isLive) {
        return new LivenessVerificationResponse(
            false,
            false,
            0.0f,
            "Liveness check failed. Please ensure you are a real person in good lighting with eyes open and face frontal.",
            false);
      }

      CompareFacesResponse compareResponse = rekognitionService.compareFaces(
          virtualPresenceData.getFaceImageData(),
          videoFrameBytes);

      List<CompareFacesMatch> faceMatches = compareResponse.faceMatches();

      if (faceMatches.isEmpty()) {
        return new LivenessVerificationResponse(
            true,
            false,
            0.0f,
            "Face does not match the reference image.",
            false);
      }

      // Get the best match
      CompareFacesMatch bestMatch = faceMatches.get(0);
      float similarity = bestMatch.similarity();

      boolean facesMatch = similarity >= 80.0f;

      if (facesMatch) {
        return new LivenessVerificationResponse(
            true,
            true,
            similarity,
            "Verification successful! Face matches and liveness confirmed.",
            true);
      } else {
        return new LivenessVerificationResponse(
            true,
            false,
            similarity,
            "Face does not match sufficiently. Similarity: " + String.format("%.2f", similarity) + "%",
            false);
      }

    } catch (Exception e) {
      return new LivenessVerificationResponse(
          false,
          false,
          0.0f,
          "Error during verification: " + e.getMessage(),
          false);
    }
  }

  public LivenessVerificationResponse verifyWithFaceLiveness(String livenessSessionId, String dateNep) {
    // Step 1: Check Face Liveness (anti-spoofing)
    float confidenceThreshold = 70.0f; // Lowered from 80 for better real-world performance
    var livenessResults = faceLivenessService.getLivenessSessionResults(livenessSessionId);
    boolean isLive = faceLivenessService.isSessionLive(livenessResults, confidenceThreshold);

    if (!isLive) {
      var msg = "Liveness verification failed. This appears to be a spoofing attempt (photo/video). Please ensure you are a real person.";
      return new LivenessVerificationResponse(false, false, 0.0f, msg, false);
    }

    // Step 2: Get reference image from liveness session
    if (livenessResults.referenceImage() == null || livenessResults.referenceImage().bytes() == null) {
      var msg = "Error: No face image captured from liveness session";
      return new LivenessVerificationResponse(true, false, 0.0f, msg, false);
    }

    byte[] liveFrameBytes = livenessResults.referenceImage().bytes().asByteArray();
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(CurrentUser.getUserName())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    // Step 3: Verify Liveness-face with virtual-presence face
    //    var virtualPresenceData = virtualPresenceRepository.findLatestBySubPNo(CurrentUser.getUserName())
    //        .orElseThrow(() -> new BadRequestException("Virtual Presence Registration Not Found"));
    //
    //    var match1Result = doesFaceImagesMatch(liveFrameBytes, virtualPresenceData.getFaceImageData());
    //    if (!match1Result.getSecond())
    //      return new LivenessVerificationResponse(true, false, 0.0f, match1Result.getFirst(), false);

    // Step 4: Verify liveness-face with nid face
    var nidRequestDto = new NinRequestDto();
    nidRequestDto.setNin(pensioner.getNationalId());
    var nidDetails = nidService.getCitizenData(nidRequestDto).block();
    if (nidDetails == null || nidDetails.getCitizenDataForPublicServiceCommision() == null)
      throw new BadRequestException("Failed To Fetch Details From NID");
    var nidImage = nidDetails.getCitizenDataForPublicServiceCommision().getPortraitImage();
    var jpegNidImage = imageConversionService.convertJp2ToJpeg(nidImage);

    var match2Result = doesFaceImagesMatch(liveFrameBytes, jpegNidImage);
    if (!match2Result.getSecond())
      return new LivenessVerificationResponse(true, false, 0.0f, match2Result.getFirst(), false);

    // step 5
    var presenceData = new PensionerPresence();
    presenceData.setPensionSubPNo(pensioner.getPensionSubPNo());
    presenceData.setPresentDate(dateNep);
    presenceData.setPresentDateEng(LocalDate.now());
    presenceData.setNid(pensioner.getNationalId());
    presenceData.setFileData(nidImage);
    presenceData.setType(2); // virtual presence
    pensionPresenceRepository.save(presenceData);

    var message = "Verification successful! Face matches and liveness confirmed (real person detected).";
    return new LivenessVerificationResponse(true, true, 100, message, true);
  }

  private Pair<String, Boolean> doesFaceImagesMatch(byte[] livenessFace, byte[] storedFace) {
    var compareResponse = rekognitionService.compareFaces(storedFace, livenessFace);
    var faceMatches = compareResponse.faceMatches();

    if (faceMatches.isEmpty())
      return Pair.of("Face does not match the reference image.", false);

    var bestMatch = faceMatches.get(0);
    float similarity = bestMatch.similarity();
    if (similarity >= 80.0f) {
      return Pair.of("Verification successful! Face matches and liveness confirmed", true);
    } else {
      return Pair.of("Face does not match sufficiently. Similarity: " + String.format("%.2f", similarity) + "%", false);
    }
  }
}
