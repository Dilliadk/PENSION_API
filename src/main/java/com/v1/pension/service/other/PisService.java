package com.v1.pension.service.other;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.v1.pension.dto.pis.PisLoginResponse;
import com.v1.pension.dto.pis.PisPensionerMainDataResponse;

@Service
@RequiredArgsConstructor
public class PisService {
  public Mono<PisPensionerMainDataResponse> getPisDataForPensioner(Map<String, Object> formData) {
    var dataUrl = "https://app.pis.gov.np/api/MainServiceApi/PIS/GetPensionItemsInfo";
    return loginToPis().flatMap(loginData -> {
      if (!loginData.isSuccess()) {
        var dataResponse = new PisPensionerMainDataResponse();
        dataResponse.setSuccess(false);
        return Mono.just(dataResponse);
      }

      return WebClient.builder()
          .baseUrl(dataUrl)
          .defaultCookie("pis.live", loginData.getServer_cookie())
          .build()
          .post()
          .uri("")
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(formData))
          .retrieve()
          .bodyToMono(PisPensionerMainDataResponse.class)
          .onErrorResume(error1 -> {
            var dataResponse = new PisPensionerMainDataResponse();
            dataResponse.setSuccess(false);
            return Mono.just(dataResponse);
          });

    });
  }

  private Mono<PisLoginResponse> loginToPis() {
    var loginUrl = "https://app.pis.gov.np/api/auth/authenticate";

    Map<String, Object> formData = new HashMap<>();
    formData.put("username", "pension_office_api");
    formData.put("password", "~HxPQq9-dddj_%");
    return WebClient.builder()
        .baseUrl(loginUrl)
        .build()
        .post()
        .uri("")
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(formData))
        .retrieve()
        .bodyToMono(PisLoginResponse.class);
  }
}
