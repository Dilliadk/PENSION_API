package com.v1.pension.service.other;

import java.util.Map;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.v1.pension.dto.nid.BiometricAuthRequestDto;
import com.v1.pension.dto.nid.CitizenDataResponse;
import com.v1.pension.dto.nid.NinRequestDto;
import com.v1.pension.dto.nid.VerifyNinResponse;
import com.v1.pension.exception.BadRequestException;

@Service
public class NidService {
  private final WebClient webClient;
  private final ImageConversionService imageConversionService;

  public NidService(ImageConversionService imageConversionService) {
    this.imageConversionService = imageConversionService;
    this.webClient = WebClient.create();
  }

  public Mono<VerifyNinResponse> verifyNin(NinRequestDto dto) {
    var path = "http://10.100.193.71:8081/nid-api/api/nid/verify-nin";

    var requestBody = Map.of("nin", dto.getNin());

    return webClient.post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new BadRequestException("Error: Invalid NIN"))))
        .bodyToMono(VerifyNinResponse.class)
        .map(response -> response);
  }

  public byte[] getNidImageData(String nin) {
    var nidRequestDto = new NinRequestDto();
    nidRequestDto.setNin(nin);
    var nidDetails = this.getCitizenData(nidRequestDto).block();
    if (nidDetails == null || nidDetails.getCitizenDataForPublicServiceCommision() == null)
      throw new BadRequestException("Failed To Fetch Details From NID");
    var nidImage = nidDetails.getCitizenDataForPublicServiceCommision().getPortraitImage();
    return imageConversionService.convertJp2ToJpeg(nidImage);
  }

  public byte[] getOriginalNidImageData(String nin) {
    var nidRequestDto = new NinRequestDto();
    nidRequestDto.setNin(nin);
    var nidDetails = this.getCitizenData(nidRequestDto).block();
    if (nidDetails == null || nidDetails.getCitizenDataForPublicServiceCommision() == null)
      throw new BadRequestException("Failed To Fetch Details From NID");
    return nidDetails.getCitizenDataForPublicServiceCommision().getPortraitImage();
  }

  public Mono<CitizenDataResponse> getCitizenData(NinRequestDto dto) {
    var path = "http://10.100.193.71:8081/nid-api/api/nid/citizen-data";

    var requestBody = Map.of("nin", dto.getNin());

    return webClient.post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new BadRequestException("Error: Invalid NIN"))))
        .bodyToMono(CitizenDataResponse.class)
        .map(response -> response);
  }

  public Mono<String> biometricAuth(BiometricAuthRequestDto dto) {
    var path = "http://10.100.193.71:8081/nid-api/api/nid/biometric-auth";

    var requestBody = Map.of(
        "nin", dto.getNin(),
        "fingerprintPosition", dto.getFingerprintPosition(),
        "imageData", dto.getImageData());

    return webClient.post()
        .uri(path)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(requestBody))
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new BadRequestException("Error: Verifying Fingerprint"))))
        .bodyToMono(String.class)
        .map(response -> response);
  }
}
