package com.v1.pension.dto.nrb;

import java.text.DecimalFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.v1.pension.exception.BadRequestException;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NrbRateDetailResponse {
  private NrbStatus status;
  private NrbRateData data;

  private boolean isFetchSuccess() {
    return status.getCode() == 200;
  }

  public double getCurrencyRate(String currencyCode) {
    if (!isFetchSuccess())
      throw new BadRequestException("Failed to fetch rates from NRB");

    if (data.getPayload() == null || data.getPayload().isEmpty())
      throw new BadRequestException("Failed to fetch rates from NRB, payload is null");

    var rates = data.getPayload().get(0).getRates().stream()
        .filter(x -> x.getCurrency().getIso3().equals(currencyCode))
        .toList();

    if (rates.isEmpty())
      throw new BadRequestException("No " + currencyCode + " rate found in NRB response");

    var rateEntity = rates.get(0);
    var rate = Double.parseDouble(rateEntity.getSell()) / rateEntity.getCurrency().getUnit();
    var df = new DecimalFormat("#.##");
    return Double.parseDouble(df.format(rate));
  }
}
