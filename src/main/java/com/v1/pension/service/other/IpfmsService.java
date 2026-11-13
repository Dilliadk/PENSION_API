package com.v1.pension.service.other;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.v1.pension.dto.ipfms.IpfmsBatchPaymentResponse;
import com.v1.pension.dto.ipfms.IpfmsLoginResponse;
import com.v1.pension.dto.ipfms.IpfmsPaymentDetail;
import com.v1.pension.dto.ipfms.IpfmsPaymentMaster;
import com.v1.pension.dto.ipfms.IpfmsStatusCheckResponse2;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.VoucherTransactionMaster;
import com.v1.pension.repository.pensioner.VoucherTransactionDetailRepository;
import com.v1.pension.repository.pensioner.VoucherTransactionMasterRepository;
import com.v1.pension.repository.system.NepaliCalendarRepository;
import com.v1.pension.repository.system.PensionerTypeRepository;

@Service
@RequiredArgsConstructor
public class IpfmsService {
  private final VoucherTransactionMasterRepository masterRepository;
  private final VoucherTransactionDetailRepository detailRepository;
  private final PensionerTypeRepository pensionerTypeRepository;
  private final NepaliCalendarRepository nepaliCalendarRepository;

  private static final Logger logger = LoggerFactory.getLogger(IpfmsService.class);

  public IpfmsPaymentMaster getIpfmsPaymentData(long masterId) {
    var dateEntity = nepaliCalendarRepository.findByRomanDate(LocalDate.now())
        .orElseThrow(() -> new ResourceNotFoundException("Date not found"));
    var master = masterRepository.findById(masterId)
        .orElseThrow(() -> new ResourceNotFoundException("Payment Info Not Found"));
    if (master.getVoucherType() != 4)
      throw new BadRequestException("this is not a payment voucher");

    List<IpfmsPaymentDetail> details = null;
    if (master.getPaymentType() == 1) {
      details = detailRepository.getRegularPaymentDetailsForIpfms(master.getId());
    } else if (master.getPaymentType() == 2) {
      details = detailRepository.getTameliPaymentDetailsForIpfms(masterId);
    } else if (master.getPaymentType() == 3) {
      details = detailRepository.getManachamalPaymentDetailsForIpfms(masterId);
    }

    if (details == null || details.isEmpty())
      throw new BadRequestException("No details row for this voucher");

    var refNo = UUID.randomUUID().toString();
    return getPaymentMaster(master, details, dateEntity.getNepaliDate(), refNo);
  }

  public void sendBulkPaymentToIpfms(long masterId, String date) {
    var master = masterRepository.findById(masterId)
        .orElseThrow(() -> new ResourceNotFoundException("Payment Info Not Found"));
    if (master.getVoucherType() != 4)
      return; // skip for other than payment

    if (master.getStage() != 4)
      throw new BadRequestException("This payment has not been approved yet");
    if (master.getStatus() == 2)
      throw new BadRequestException("This payment voucher has already been sent to ipfms");

    List<IpfmsPaymentDetail> details = null;
    if (master.getPaymentType() == 1) {
      details = detailRepository.getRegularPaymentDetailsForIpfms(master.getId());
    } else if (master.getPaymentType() == 2) {
      details = detailRepository.getTameliPaymentDetailsForIpfms(masterId);
    } else if (master.getPaymentType() == 3) {
      details = detailRepository.getManachamalPaymentDetailsForIpfms(masterId);
    }

    if (details == null || details.isEmpty())
      return;

    var refNo = UUID.randomUUID().toString();
    var ipfmsPaymentData = getPaymentMaster(master, details, date, refNo);
    // var res = sendBulkPayment(master, details, date, refNo).block();
    var res = sendBulkPayment(ipfmsPaymentData).block();
    if (res == null) {
      logger.error("Response for IPFMS is null");
      throw new BadRequestException("Failed: Ipfms payment");
    }

    logger.info("IPFMS Result: status = {}, message = {}", res.isSuccess(), res.getMessage());
    if (res.isSuccess()) {
      master.setStatus(2); // processing (by ipfms)
      master.setRefBatchNo(refNo);
      masterRepository.save(master);

      var detailIds = details.stream().map(IpfmsPaymentDetail::getPensionPaymentDetailId).toList();
      detailRepository.updateDetailsStatus(detailIds, 2);
    } else {
      throw new BadRequestException(res.getMessage());
    }
  }

  private IpfmsPaymentMaster getPaymentMaster(
      VoucherTransactionMaster master, List<IpfmsPaymentDetail> details, String date, String refNo) {
    var pensionerType = pensionerTypeRepository.findByTypeCode(master.getPensionerTypeCode())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Type Not Found"));

    var data = new IpfmsPaymentMaster();
    data.setCategoryTypeId(Long.parseLong(pensionerType.getTypeCode()));
    data.setAccountCode(pensionerType.getAccountCode());
    data.setEconomicCode(pensionerType.getEconomicCode());
    data.setActivityCode(pensionerType.getActivityCode());

    data.setRefBatchNo(refNo);
    data.setPensionPaymentId(master.getId());
    data.setBudYear(master.getFiscalYear());
    data.setTranDate(master.getStatementDate());
    data.setCreatedDate(date);

    data.setDetails(getPaymentRows(details));
    return data;
  }

  private List<Map<String, Object>> getPaymentRows(List<IpfmsPaymentDetail> details) {
    List<Map<String, Object>> rows = new ArrayList<>();
    for (var d : details) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("pensionPaymentDetailId", d.getPensionPaymentDetailId());
      row.put("pan", d.getPan());
      row.put("payeeCode", d.getPayeeCode());
      row.put("ename", d.getEname());
      row.put("nname", d.getNname());
      row.put("totalAmount", d.getTotalAmount() * -1);
      row.put("socialTaxAmount", d.getSocialTaxAmount());
      row.put("tdsAmount", d.getTdsAmount() * -1);
      row.put("payableAmount", d.getPayableAmount() * -1);
      row.put("bankId", d.getBankId());
      row.put("bankAccountNo", d.getBankAccountNo());
      rows.add(row);
    }
    return rows;
  }

  private Mono<IpfmsBatchPaymentResponse> sendBulkPayment(IpfmsPaymentMaster data) {
    var dataUrl = "https://ipfms.fcgo.gov.np:6789/ipfm-integration/api/bulk-upload/save";
    return loginToIpfms().flatMap(loginData -> {
      if (loginData.getAccess_token() == null && loginData.getAccess_token().isBlank()) {
        var dataResponse = new IpfmsBatchPaymentResponse();
        dataResponse.setSuccess(false);
        dataResponse.setMessage("Login to ipfms failed");
        return Mono.just(dataResponse);
      }

      int bufferSize = 10 * 1024 * 1024;

      return WebClient.builder()
          .baseUrl(dataUrl)
          .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(bufferSize))
          .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + loginData.getAccess_token())
          .build()
          .post()
          .uri("")
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(data))
          .retrieve()
          .bodyToMono(IpfmsBatchPaymentResponse.class)
          .onErrorResume(error1 -> {
            var dataResponse = new IpfmsBatchPaymentResponse();
            dataResponse.setSuccess(false);
            dataResponse.setMessage("Bulk Payment Failed");
            return Mono.just(dataResponse);
          });
    });
  }

  public Mono<IpfmsStatusCheckResponse2> checkPaymentStatus(VoucherTransactionMaster vm) {
    if (vm.getVoucherType() != 4)
      return Mono.just(generateStatusCheckErrorResponse2("This voucher is not payment type"));
    if (vm.getStage() != 4)
      return Mono.just(generateStatusCheckErrorResponse2("This voucher has not been approved"));
    if (vm.getStatus() == 1)
      return Mono.just(generateStatusCheckErrorResponse2("This voucher has not been sent for eft"));

    var type = pensionerTypeRepository.findByTypeCode(vm.getPensionerTypeCode())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Type Not Found"));

    var statusCheckUrl = "https://ipfms.fcgo.gov.np:6789/ipfm-integration/api/bulk-upload/get-status";
    return loginToIpfms().flatMap(loginData -> {
      if (loginData.getAccess_token() == null && loginData.getAccess_token().isBlank()) {
        return Mono.just(generateStatusCheckErrorResponse2("Login to ipfms failed"));
      }

      int bufferSize = 25 * 1024 * 1024;

      Map<String, Object> data = new HashMap<>();
      data.put("budYear", vm.getFiscalYear());
      data.put("provncCode", "0");
      data.put("treasuryOfficeId", "78");
      data.put("accountCode", type.getAccountCode());
      data.put("agencyCode", "305013502");
      data.put("batchId", vm.getRefBatchNo());
      data.put("start", "0");
      data.put("length", "50");

      return WebClient.builder()
          .baseUrl(statusCheckUrl)
          .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(bufferSize))
          .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + loginData.getAccess_token())
          .build()
          .post()
          .uri("")
          .contentType(MediaType.APPLICATION_JSON)
          .body(BodyInserters.fromValue(data))
          .retrieve()
          .bodyToMono(String.class)
          .onErrorResume(error1 -> null)
          .map(res -> {
            var output = new IpfmsStatusCheckResponse2();
            output.setSuccess(res != null && !res.isBlank());
            output.setMessage(output.isSuccess() ? "status check successful" : "status check failed");
            output.setJsonData(res);
            return output;
          });
    });
  }

  private IpfmsStatusCheckResponse2 generateStatusCheckErrorResponse2(String message) {
    var dataResponse = new IpfmsStatusCheckResponse2();
    dataResponse.setSuccess(false);
    dataResponse.setMessage(message);
    return dataResponse;
  }

  private Mono<IpfmsLoginResponse> loginToIpfms() {
    String loginUrl = "https://ipfms.fcgo.gov.np:6789/auth/realms/ThirdPartyIntegrator/protocol/openid-connect/token";

    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("client_id", "pension");
    formData.add("grant_type", "password");
    formData.add("username", "pension");
    formData.add("password", "Q9cOdmNnj7AziDNrsgZPRvg5mw2NHYU8");
    formData.add("client_secret", "6r7CHar6nc2SxGmeeHZZZxXNS38wufzV");

    return WebClient.builder()
        .baseUrl(loginUrl)
        .build()
        .post()
        .uri("")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(BodyInserters.fromFormData(formData))
        .retrieve()
        .bodyToMono(IpfmsLoginResponse.class);
  }
}
