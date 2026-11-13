package com.v1.pension.controller.pensioner;

import java.io.IOException;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.pis.PidNumberDto;
import com.v1.pension.dto.pis.PisNumberDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.GeneralServerException;
import com.v1.pension.model.pensioner.BankAccount;
import com.v1.pension.model.pensioner.BankTransferLog;
import com.v1.pension.model.pensioner.NidAndOtherDetail;
import com.v1.pension.model.pensioner.PensionerBlockRelease;
import com.v1.pension.model.pensioner.PensionerPresence;
import com.v1.pension.model.pensioner.PensionerRegistration;
import com.v1.pension.model.pensioner.PensionerVirtualPresence;
import com.v1.pension.service.pensioner.BankAccountService;
import com.v1.pension.service.pensioner.BankTransferLogService;
import com.v1.pension.service.pensioner.NidAndOtherDetailService;
import com.v1.pension.service.pensioner.PensionPresenceService;
import com.v1.pension.service.pensioner.PensionerBlockReleaseService;
import com.v1.pension.service.pensioner.PensionerRegistrationService;
import com.v1.pension.service.pensioner.PensionerVirtualPresenceService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/pensioner-registration")
@RequiredArgsConstructor
public class PensionerRegistrationController {
  private final PensionerRegistrationService pensionerRegistrationService;
  private final PensionerVirtualPresenceService virtualPresenceService;
  private final BankAccountService bankAccountService;
  private final NidAndOtherDetailService nidAndOtherDetailService;
  private final PensionPresenceService pensionPresenceService;
  private final PensionerBlockReleaseService pensionerBlockReleaseService;
  private final BankTransferLogService bankTransferLogService;

  //  @GetMapping("/{id}/history")
  //  public ResponseEntity<?> getPensionerHistory(@PathVariable long id) {
  //    AuditReader auditReader = AuditReaderFactory.get(entityManager);
  //    AuditQuery query = auditReader.createQuery()
  //        .forRevisionsOfEntity(PensionerRegistration.class, true, true)
  //        .add(AuditEntity.id().eq(id));
  //
  //    List<Object[]> results = query.getResultList();
  //
  //    return ResponseEntity.ok(results);
  //  }

  @GetMapping("/has-permission-over-pensioner")
  public ResponseEntity<?> hasPermissionOverPensioner(@RequestParam String subPNo) {
    pensionerRegistrationService.hasPermissionOverPensioner(subPNo);
    return ResponseEntity.ok(MyResponse.message("You have permission over this pensioner"));
  }

  @GetMapping("/active")
  public ResponseEntity<?> getActivePensioners() {
    var data = pensionerRegistrationService.getActivePensioners();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getPensionerById(@PathVariable long id) {
    var data = pensionerRegistrationService.getById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/main/save")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> saveMainPensioner(@RequestBody PensionerRegistration request) {
    pensionerRegistrationService.saveMainPensioner(request);
    return ResponseEntity.ok(MyResponse.message("Main pensioner has been saved"));
  }

  @GetMapping("/main/{pensionNo}")
  public ResponseEntity<?> getMainPensioner(@PathVariable String pensionNo) {
    var data = pensionerRegistrationService.findByPensionNo(pensionNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping("/sub/save")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> saveSubPensioner(@RequestBody PensionerRegistration request) {
    pensionerRegistrationService.saveSubPensioner(request);
    return ResponseEntity.ok(MyResponse.message("Sub pensioner has been saved"));
  }

  @GetMapping("/sub/all")
  public ResponseEntity<?> getSubPensionersByPensionNo(@RequestParam String pensionNo) {
    var data = pensionerRegistrationService.getSubPensionersByPensionNo(pensionNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping("/sub/direct/save")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> saveSubDirectPensioner(@RequestBody PensionerRegistration request) {
    pensionerRegistrationService.saveDirectSubPensioner(request);
    return ResponseEntity.ok(MyResponse.message("Sub pensioner has been saved"));
  }

  @GetMapping("/sub/direct/{pensionNo}")
  public ResponseEntity<?> getDirectSubPensioner(@PathVariable String pensionNo) {
    var data = pensionerRegistrationService.getDirectSubPensioner(pensionNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-info")
  public ResponseEntity<?> getPensionerBySubPNo(@RequestParam String subPNo) {
    var data = pensionerRegistrationService.getPensionerInfoBySubPNo(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  // ----------- Virtual Presence -------------------------------------
  @GetMapping("/virtual-presence")
  public ResponseEntity<?> getVirtualPresenceDataBySubPNo(@RequestParam String subPNo) {
    var data = virtualPresenceService.getAllBySubPNo(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping("/virtual-presence")
  public ResponseEntity<?> addVirtualPresence(@ModelAttribute PensionerVirtualPresence request) {
    try {
      virtualPresenceService.addVirtualPresence(request);
      return ResponseEntity.ok(MyResponse.message("Virtual Presence Info Has Been Saved"));
    } catch (IOException e) {
      throw new BadRequestException("Failed to upload pensioner document");
    }
  }

  // ------------ Bank Account -------------------------------------------
  @GetMapping("/bank-accounts")
  public ResponseEntity<?> getPensionerBankAccounts(@RequestParam String subPNo) {
    var data = bankAccountService.getAllBySubPNo(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/bank-accounts/{id}")
  public ResponseEntity<?> getById(@PathVariable long id) {
    var data = bankAccountService.getById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/bank-accounts/save")
  public ResponseEntity<?> savePensionerBankAccount(@RequestBody BankAccount request) {
    bankAccountService.savePensionerBankAccount(request);
    return ResponseEntity.ok(MyResponse.message("Bank Account Has Been Saved"));
  }

  @GetMapping("/bank-accounts/close")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> closeBankAccountsForPensioner(@RequestParam String subPNo) {
    bankAccountService.closeBankAccountForPensioner(subPNo, 2);
    return ResponseEntity.ok(MyResponse.message("Account Has Been Closed Successfully"));
  }

  // ------------ Bank Transfer -------------------------------------------------
  @GetMapping("/bank-transfer-logs")
  public ResponseEntity<?> getPensionerBankTransferLogs(@RequestParam String subPNo) {
    var data = bankTransferLogService.getPensionerBankTransferLogs(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping("/bank-transfer-logs/save")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> saveBankTransferLog(@RequestBody BankTransferLog request) {
    bankTransferLogService.transferBank(request);
    return ResponseEntity.ok(MyResponse.message("Bank Transfer Success"));
  }

  // ------------ NID And Other Detail -------------------------------------------
  @GetMapping("/nid-and-other-details")
  public ResponseEntity<?> getPensionerNidAndOtherDetails(@RequestParam String subPNo) {
    var data = nidAndOtherDetailService.getAllBySubPNo(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/nid-and-other-details/{id}")
  public ResponseEntity<?> getNidAndOtherDetailById(@PathVariable long id) {
    var data = nidAndOtherDetailService.getById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/nid-and-other-details/save")
  public ResponseEntity<?> saveNidAndOtherDetails(@RequestBody NidAndOtherDetail request) {
    nidAndOtherDetailService.savePensionerNidAndOtherDetails(request);
    return ResponseEntity.ok(MyResponse.message("NID And Other Details Have Been Saved"));
  }

  // ------------ Pension Presence -------------------------------------------
  @GetMapping("/pension-presence")
  public ResponseEntity<?> getPensionPresence(@RequestParam String subPNo, @RequestParam int type) {
    var data = pensionPresenceService.getAllBySubPNo(subPNo, type);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pension-presence/{id}")
  public ResponseEntity<?> getPensionerPresenceById(@PathVariable long id) {
    var data = pensionPresenceService.getById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/pension-presence/save")
  public ResponseEntity<?> savePensionPresence(@ModelAttribute PensionerPresence request) {
    try {
      pensionPresenceService.savePensionerPresence(request);
      return ResponseEntity.ok(MyResponse.message("Pension Presence Has Been Saved"));
    } catch (IOException e) {
      throw new BadRequestException("Could not save image");
    }
  }

  @GetMapping("/pension-presence/{id}/view")
  public ResponseEntity<byte[]> viewPresenceDocumentById(@PathVariable Long id) {
    var presenceInfo = pensionPresenceService.getById(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(presenceInfo.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + presenceInfo.getPensionSubPNo() + "\"")
        .body(presenceInfo.getFileData());
  }

  @GetMapping("/pension-presence/download-anusuchi-2")
  public ResponseEntity<byte[]> downloadAnusuchi2() {
    var res = new ClassPathResource("downloadable-files/anusuchi2.pdf");
    try (var is = res.getInputStream()) {
      return ResponseEntity.ok()
          .contentType(MediaType.APPLICATION_PDF)
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "anusuchi2.pdf" + "\"")
          .body(is.readAllBytes());
    } catch (IOException e) {
      throw new GeneralServerException("Failed to load file");
    }
  }

  // ------------ Pensioner Block Release -------------------------------------------
  @GetMapping("/block-release")
  public ResponseEntity<?> getPensionerBlockRelease(@RequestParam String subPNo) {
    var data = pensionerBlockReleaseService.getPensionerBlockReleases(subPNo);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/block-release/{id}")
  public ResponseEntity<?> getPensionerBlockReleaseById(@PathVariable long id) {
    var data = pensionerBlockReleaseService.getById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/block-release/save")
  public ResponseEntity<?> savePensionerBlockRelease(@RequestBody PensionerBlockRelease request) {
    pensionerBlockReleaseService.saveBlockRelease(request);
    return ResponseEntity.ok(MyResponse.message("Pension Block/Release Has Been Saved"));
  }

  // PIS Integration
  @PostMapping("/search-with-pis-number")
  public ResponseEntity<?> searchWithPisNo(@RequestBody PisNumberDto dto) {
    var data = pensionerRegistrationService.searchWithPis(dto.getPisNo());
    return ResponseEntity.ok(data);
  }

  @PostMapping("/search-with-pid-number")
  public ResponseEntity<?> searchWithPidNo(@RequestBody PidNumberDto dto) {
    var data = pensionerRegistrationService.searchWithPid(dto.getPidNo());
    return ResponseEntity.ok(data);
  }

  @PostMapping("/search-with-pis-number-raw")
  public ResponseEntity<?> searchWithPisNoRaw(@RequestBody PisNumberDto dto) {
    var data = pensionerRegistrationService.searchWithPisRaw(dto.getPisNo());
    return ResponseEntity.ok(data);
  }

  @PostMapping("/search-with-pid-number-raw")
  public ResponseEntity<?> searchWithPidNoRaw(@RequestBody PidNumberDto dto) {
    var data = pensionerRegistrationService.searchWithPidRaw(dto.getPidNo());
    return ResponseEntity.ok(data);
  }
}