package com.v1.pension.service.other;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.v1.pension.dto.nrb.NrbRateDetailResponse;
import com.v1.pension.exception.BadRequestException;

@Service
public class NrbService {
  private final RestClient restClient;

  private final Logger logger = LoggerFactory.getLogger(NrbService.class);

  NrbService() {
    this.restClient = RestClient.create();
  }

  public double getTodayRateForCurrency(String currencyCode) {
    var today = LocalDate.now().toString();
    var url = String.format("https://www.nrb.org.np/api/forex/v1/rates?page=1&per_page=100&from=%s&to=%s", today,
        today);

    var ratesResponse = restClient.get()
        .uri(url)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (req, res) -> {
          String body = new String(res.getBody().readAllBytes());
          logger.error("Currency Rates Fetch Failed: {}", body);
          throw new BadRequestException("Failed to fetch rates from NRB");
        })
        .body(NrbRateDetailResponse.class);

    if (ratesResponse == null)
      return 1;

    return ratesResponse.getCurrencyRate(currencyCode);
  }
}
