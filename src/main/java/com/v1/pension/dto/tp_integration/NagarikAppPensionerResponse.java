package com.v1.pension.dto.tp_integration;

import java.util.List;
import lombok.Data;

import com.v1.pension.dto.pensioner.presence.PensionerPresenceSummary;

@Data
public class NagarikAppPensionerResponse {
  private PensionerInfoForNagarikApp pensionerInfo;
  private List<PensionerPresenceSummary> presenceHistory;
}
