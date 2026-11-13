package com.v1.pension.controller.pensioner;

import java.util.UUID;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.liveness_prixa.LivenessResultDto;
import com.v1.pension.dto.liveness_prixa.PrixaJwtResponse;
import com.v1.pension.dto.liveness_prixa.PrixaLivenessResult;
import com.v1.pension.dto.tp_integration.NagarikAppLivenessInitiateDto;
import com.v1.pension.dto.tp_integration.NagarikAppPensionerResponse;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.service.liveness_prixa.PrixaLivenessService;
import com.v1.pension.service.pensioner.PensionerRegistrationService;
import com.v1.pension.utils.CurrentUser;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/liveness-verification")
@RequiredArgsConstructor
public class LivenessVerificationController {
  private final PrixaLivenessService prixaLivenessService;
  private final PensionerRegistrationService pensionerRegistrationService;

  @GetMapping("/initiate")
  @PreAuthorize("hasAuthority('PENSIONER')")
  public ResponseEntity<PrixaJwtResponse> initiateLivenessVerification() {
    var data = prixaLivenessService.getKycUrl(CurrentUser.getUserName(), null);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/process-result")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> processLivenessResult(@RequestBody PrixaLivenessResult result) {
    prixaLivenessService.validatePrixaJwt(result.getJwt());
    prixaLivenessService.processLivenessResult(result);

    return ResponseEntity.ok(MyResponse.message("Liveness Result Has Been Processed"));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyAuthority('PENSIONER', 'NAGARIK_APP')")
  public ResponseEntity<LivenessResultDto> getById(@PathVariable UUID id) {
    var data = prixaLivenessService.getResponseById(id);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/my-info")
  @PreAuthorize("hasAuthority('PENSIONER')")
  public ResponseEntity<NagarikAppPensionerResponse> getMyInfo() {
    var data = pensionerRegistrationService.getPensionerInfoForNagarikApp(CurrentUser.getUserName());
    return ResponseEntity.ok(data);
  }

  @GetMapping("/nagarik-app")
  @PreAuthorize("hasAuthority('NAGARIK_APP')")
  public ResponseEntity<NagarikAppPensionerResponse> getPensionerInfo(@RequestParam String pensionNo) {
    var data = pensionerRegistrationService.getPensionerInfoForNagarikApp(pensionNo);
    return ResponseEntity.ok(data);
  }

  @PostMapping("/nagarik-app/initiate")
  @PreAuthorize("hasAuthority('NAGARIK_APP')")
  public ResponseEntity<PrixaJwtResponse> initiateFromNagarikApp(
      @Valid @RequestBody NagarikAppLivenessInitiateDto dto) {
    var isKycValid = pensionerRegistrationService.isKycValid(dto.getPensionNo());
    if (isKycValid)
      throw new BadRequestException("Liveness verification is not required, your kyc is already valid");

    var data = prixaLivenessService.getKycUrl(dto.getPensionNo(), dto.getReturnTo());
    return ResponseEntity.ok(data);
  }

  // @GetMapping("/original-nid-image")
  // public ResponseEntity<byte[]> getOriginalNidImage(@RequestParam String identifier) {
  //   var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(identifier)
  //       .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

  //   var imageData = nidService.getNidImageData(pensioner.getNationalId());
  //   return ResponseEntity.ok()
  //       .contentType(MediaType.IMAGE_JPEG)
  //       .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + identifier + ".jp2" + "\"")
  //       .body(imageData);
  // }
}
