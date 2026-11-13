package com.v1.pension.service.liveness_prixa;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.crypto.SecretKey;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.v1.pension.dto.liveness_prixa.BytesRequest;
import com.v1.pension.dto.liveness_prixa.LivenessResultDto;
import com.v1.pension.dto.liveness_prixa.PrixaFaceComparisonResult;
import com.v1.pension.dto.liveness_prixa.PrixaImageComparisonRequest;
import com.v1.pension.dto.liveness_prixa.PrixaJwtError;
import com.v1.pension.dto.liveness_prixa.PrixaJwtResponse;
import com.v1.pension.dto.liveness_prixa.PrixaLivenessResult;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.LivenessResultEntity;
import com.v1.pension.model.pensioner.PensionerPresence;
import com.v1.pension.model.pensioner.PensionerRegistration;
import com.v1.pension.repository.pensioner.LivenessResultRepository;
import com.v1.pension.repository.pensioner.PensionPresenceRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.system.NepaliCalendarRepository;
import com.v1.pension.service.other.NidService;

@Service
public class PrixaLivenessService {
  private final SecretKey signingKey;
  private final PensionerRegistrationRepository pensionerRepository;
  private final PensionPresenceRepository pensionPresenceRepository;
  private final NepaliCalendarRepository nepaliCalendarRepository;
  private final LivenessResultRepository livenessResultRepository;
  private final NidService nidService;
  private final RestClient restClient;
  private final SimpMessagingTemplate messagingTemplate;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ModelMapper modelMapper;
  private final Logger logger = LoggerFactory.getLogger(PrixaLivenessService.class);

  @Value("${prixa.callback_url}")
  private String prixaCallbackUrl;

  @Value("${prixa.return_url}")
  private String prixaReturnUrl;

  public PrixaLivenessService(@Value("${prixa.secret_key}") String prixaSecretKey,
      PensionerRegistrationRepository pRepo, PensionPresenceRepository pensionPresenceRepository,
      NepaliCalendarRepository nepaliCalendarRepository, NidService nidService,
      SimpMessagingTemplate messagingTemplate, LivenessResultRepository livenessResultRepository,
      ModelMapper modelMapper) {
    this.signingKey = Keys.hmacShaKeyFor(prixaSecretKey.getBytes());
    this.pensionerRepository = pRepo;
    this.pensionPresenceRepository = pensionPresenceRepository;
    this.nepaliCalendarRepository = nepaliCalendarRepository;
    this.nidService = nidService;
    this.messagingTemplate = messagingTemplate;
    this.livenessResultRepository = livenessResultRepository;
    this.modelMapper = modelMapper;
    this.restClient = RestClient.create();
  }

  public LivenessResultDto getResponseById(UUID id) {
    var entity = livenessResultRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Liveness result not found"));

    return modelMapper.map(entity, LivenessResultDto.class);
  }

  public void processLivenessResult(PrixaLivenessResult result) {
    var resultId = result.getSecondary_label();
    var resultEntity = livenessResultRepository.findById(UUID.fromString(resultId))
        .orElseThrow(() -> new ResourceNotFoundException("Result Information Not Found"));
    resultEntity.setStatus(2); // processing
    livenessResultRepository.save(resultEntity);

    // var webSocketTopic = "/topic/liveness-result/" + resultId;
    // messagingTemplate.convertAndSend(webSocketTopic, modelMapper.map(resultEntity, LivenessResultDto.class));

    var pensionSubPNo = result.getIdentifier();
    boolean isGestureValid = false;
    boolean isFaceValid = false;

    for (var g : result.getGestureDetectionLog()) {
      isGestureValid = g.isVerified();
    }
    for (var d : result.getFaceDetectionLog()) {
      isFaceValid = d.isVerified();
    }

    var nin = pensionerRepository.getNationalIdForPensioner(pensionSubPNo);
    var comparisonResult = sendFaceComparisonRequest(nin, result.getUserPhoto());
    if (comparisonResult != null) {
      var faceMatchPercentage = comparisonResult.getSimilarityPercentage();
      var faceComparisonValid = faceMatchPercentage > 80;

      resultEntity.setFaceValid(isFaceValid);
      resultEntity.setGestureValid(isGestureValid);
      resultEntity.setNidFaceMatchPercentage(faceMatchPercentage);
      resultEntity.setPensionSubPNo(pensionSubPNo);

      var dateEntityOpt = nepaliCalendarRepository.findByRomanDate(LocalDate.now());
      if (dateEntityOpt.isEmpty()) {
        logger.error("Date {} not found in database", LocalDate.now());
        return;
      }

      if (isGestureValid && isFaceValid && faceComparisonValid) {
        var pensioner = pensionerRepository.findByPensionSubPNo(pensionSubPNo)
            .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

        var presenceData = new PensionerPresence();
        presenceData.setPensionSubPNo(pensionSubPNo);
        presenceData.setPresentDate(dateEntityOpt.get().getNepaliDate());
        presenceData.setPresentDateEng(dateEntityOpt.get().getRomanDate());
        presenceData.setNid(nin);
        presenceData.setFileData(Base64.getDecoder().decode(result.getUserPhoto().split(",")[1]));
        presenceData.setType(2); // virtual presence
        pensionPresenceRepository.save(presenceData);

        resultEntity.setPensionerPresence(presenceData);

        if (pensioner.getPensionStatus() == 2) {
          pensioner.setPensionStatus(1); // active inactive pensioner
          pensionerRepository.save(pensioner);
        }
      } else {
        logger.info(
            "Liveness Failed for {}: Face: {}, Gesture: {}, NID: {}, Match Percent: {}",
            pensionSubPNo, isFaceValid, isGestureValid, faceComparisonValid, faceMatchPercentage);
      }

      resultEntity.setStatus(3);
      livenessResultRepository.save(resultEntity);

      // var resultResponse = modelMapper.map(resultEntity, LivenessResultDto.class);
      // messagingTemplate.convertAndSend(webSocketTopic, resultResponse);
    }
  }

  private PrixaFaceComparisonResult sendFaceComparisonRequest(String nin, String livenessImageString) {
    var nidImage = nidService.getNidImageData(nin);
    var nidImageString = Base64.getEncoder().encodeToString(nidImage);
    var nidImageStringWithPrefix = "data:image/jpeg;base64," + nidImageString;
    var url = "https://faceapi.fcgo.gov.np/compare-faces";

    var sourceImage = new BytesRequest();
    sourceImage.setBytes(nidImageStringWithPrefix);
    var targetImage = new BytesRequest();
    targetImage.setBytes(livenessImageString);

    var requestDto = new PrixaImageComparisonRequest();
    requestDto.setSimilarityThreshold(0.1);
    requestDto.setSourceImage(sourceImage);
    requestDto.setTargetImage(targetImage);

    return restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .header("Authorization", "AKIAIOSFODNN7ADOIASDU wJalrXUtnFEMI/K7MDENG/bPxRfiC23498D")
        .header("Cookie", "csrftoken=b2YosflRayjxHLlwZlTH1KBBNg3aEAfP; sessionid=g57e8puzsq0mq1rf5c3saq183kr4tukh")
        .header("Accept", "application/json")
        .body(requestDto)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
          String body = new String(res.getBody().readAllBytes());
          logger.error("1:1 Face comparison failed: {}", body);
          throw new BadRequestException("Failed to compare face");
        })
        .body(PrixaFaceComparisonResult.class);
  }

  private Pair<String, String> createPrixaSdkTokenForPensioner(PensionerRegistration pensioner, String returnTo) {
    var name = pensioner.getPensionSubPensioner() == 0 ? pensioner.getPensionNameNep()
        : pensioner.getPensionSubNameNep();
    var livenessResult = new LivenessResultEntity();
    livenessResultRepository.save(livenessResult);
    var uuid = livenessResult.getId();

    var returnUrl = returnTo != null ? returnTo : prixaReturnUrl + "/" + uuid.toString();

    var jwtBuilder = Jwts.builder()
        .setSubject(pensioner.getPensionSubPNo())
        .claim("name", name)
        .claim("token", "YKbuTXTVAtIOwzeWDVvl9O_dAItBZPflwOnHFPiPVHw")
        .claim("identifier", pensioner.getPensionSubPNo())
        .claim("label", name)
        .claim("secondary_label", uuid.toString())
        // .claim("callback", "https://webhook.site/11e49b78-7540-4467-89df-190e679e18bb")
        // .claim("return_url", "http://localhost:4200/#/liveness-result/" + uuid.toString())
        .claim("callback", prixaCallbackUrl)
        .claim("return_url", returnUrl)
        .claim("is_sdk", false)
        .setIssuer("PENSIONGOV")
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)));

    var sdkToken = jwtBuilder.signWith(signingKey, SignatureAlgorithm.HS256).compact();
    return Pair.of(sdkToken, uuid.toString());
  }

  public PrixaJwtResponse getKycUrl(String subPNo, String returnTo) {
    var pensioner = pensionerRepository.findByPensionSubPNo(subPNo)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    var url = "https://verify.fcgo.gov.np/tfauth/get-kyc-url/";
    var sdkTokenRes = createPrixaSdkTokenForPensioner(pensioner, returnTo);
    var sdkToken = sdkTokenRes.getFirst();

    var data = Map.of("jwt_token", sdkToken);

    var kycRes = restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_JSON)
        .body(data)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
          try {
            String body = new String(res.getBody().readAllBytes());
            logger.error("KYC URL Generation Failed: {}", body);
            var errorResponse = objectMapper.readValue(body, PrixaJwtError.class);
            throw new BadRequestException(errorResponse.getError());
          } catch (JsonProcessingException e) {
            logger.error("Failed to parse prixa error response: {}", e.getMessage());
            throw new BadRequestException("Failed to parse error response");
          }
        })
        .body(PrixaJwtResponse.class);

    if (kycRes == null)
      throw new BadRequestException("KYC URL Generation Failed");

    kycRes.setId(sdkTokenRes.getSecond());
    return kycRes;
  }

  public void validatePrixaJwt(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    } catch (Exception e) {
      logger.error("Prixa Liveness JWT Error: {}", e.getMessage());
    }
  }
}
