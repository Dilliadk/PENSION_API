package com.v1.pension.controller.other;

import com.v1.pension.dto.nid.BiometricAuthRequestDto;
import com.v1.pension.dto.nid.NinRequestDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.service.other.ImageConversionService;
import com.v1.pension.service.other.NidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/nid")
@RequiredArgsConstructor
public class NidController {
  private final NidService nidService;
  private final ImageConversionService imageConversionService;

  @PostMapping("/verify-nin")
  public ResponseEntity<?> verifyNin(@Valid @RequestBody NinRequestDto dto) {
    var data = nidService.verifyNin(dto).block();
    return ResponseEntity.ok(data);
  }

  @PostMapping("/citizen-data")
  public ResponseEntity<?> getCitizenData(@Valid @RequestBody NinRequestDto dto) {
    var data = nidService.getCitizenData(dto).block();
    if (data != null) {
      var citizenData = data.getCitizenDataForPublicServiceCommision();
      if (citizenData != null && citizenData.getPortraitImage() != null) {
        var jpeg = imageConversionService.convertJp2ToJpeg(citizenData.getPortraitImage());
        citizenData.setPortraitImageJpeg(jpeg);
      }
    }
    return ResponseEntity.ok(data);
  }

  @PostMapping("/biometric-auth")
  public ResponseEntity<?> biometricAuth(@Valid @RequestBody BiometricAuthRequestDto dto) {
    var data = nidService.biometricAuth(dto).block();
    if (data == null) throw new BadRequestException("Empty Response From Server");
    return ResponseEntity.ok(Map.of("result", data));
  }
}
