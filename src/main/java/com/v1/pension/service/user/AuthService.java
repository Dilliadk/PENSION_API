package com.v1.pension.service.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.v1.pension.config.JwtTokenUtil;
import com.v1.pension.dto.auth.LivenessRegistrationRequestDto;
import com.v1.pension.dto.auth.LoginRequestDto;
import com.v1.pension.dto.auth.LoginResponseDto;
import com.v1.pension.dto.auth.OtpRequestDto;
import com.v1.pension.dto.auth.OtpVerificationDto;
import com.v1.pension.dto.auth.RoleRequestDto;
import com.v1.pension.dto.auth.RoleResponseDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.auth.OtpVerification;
import com.v1.pension.model.auth.UserDevice;
import com.v1.pension.model.user.LivenessRegistrationRequest;
import com.v1.pension.model.user.User;
import com.v1.pension.repository.auth.OtpVerificationRepository;
import com.v1.pension.repository.auth.UserDeviceRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.user.LivenessRegistrationRequestRepository;
import com.v1.pension.repository.user.UserRepository;
import com.v1.pension.service.other.EmailService;
import com.v1.pension.service.other.SmsService;
import com.v1.pension.service.pensioner.PensionerRegistrationService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
  private final JwtTokenUtil jwtTokenUtil;
  private final OtpVerificationRepository otpVerificationRepository;
  private final BCryptPasswordEncoder bCryptEncoder;
  private final UserRepository userRepository;
  private final UserDeviceRepository userDeviceRepository;
  private final SmsService smsService;
  private final PensionerRegistrationService pensionerRegistrationService;
  private final LivenessRegistrationRequestRepository livenessRegistrationRequestRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final EmailService emailService;

  // public void changeThirdPartyPassword(String username) {
  //   var user = userRepository.getUserByUsername(username)
  //       .orElseThrow(() -> new ResourceNotFoundException("user not found"));

  //   var newPassword = bCryptEncoder.encode("");
  //   user.setPasswordHash(newPassword);
  //   userRepository.save(user);
  // }

  public long livenessRegistration(LivenessRegistrationRequestDto dto) {
    var pensionNumber = dto.getPensionNumber().strip();
    var nid = dto.getNationalId().strip();
    var retireDate = dto.getRetiredDate().strip();
    var mob = dto.getMobileNumber().strip();
    var confirmMob = dto.getConfirmMobileNumber().strip();

    if (!mob.equals(confirmMob))
      throw new BadRequestException("कृपया मोवाईल नं. एउटै राख्नुहोस् ।");

    var pensioner = pensionerRegistrationService.getByPensionSubPNo(pensionNumber);
    if (pensioner.getVirtualPresenceStatus() == 1)
      throw new BadRequestException("You have already been registered for virtual presence");
    if (pensioner.getNationalId() == null || !pensioner.getNationalId().equals(nid))
      throw new BadRequestException("तपाइको राष्ट्रिय परिचय पत्र नं. मिलेन");
    if (pensioner.getPensionRetireNep() == null || !pensioner.getPensionRetireNep().equals(retireDate))
      throw new BadRequestException("तपाइको अवकाश मिति मिलेन");

    var entity = new LivenessRegistrationRequest();
    entity.setPensionNumber(pensionNumber);
    entity.setNationalId(nid);
    entity.setRetiredDate(retireDate);
    entity.setMobileNumber(mob);

    var otp = generateOtp();
    var msg = String.format("Use %s as a verification code for Pension Management System. OTP Valid for 2 minutes",
        otp);
    var isSmsSent = smsService.sendOtpMessage(dto.getMobileNumber(), msg);
    if (!isSmsSent)
      throw new BadRequestException("Error sending sms to " + dto.getMobileNumber());

    entity.setHashedOtp(bCryptEncoder.encode(otp));
    entity.setOtpValidUntil(LocalDateTime.now().plusMinutes(2));
    livenessRegistrationRequestRepository.save(entity);
    return entity.getId();
  }

  public void verifyOtpForLivenessVerification(long id, OtpRequestDto dto) {
    var otp = dto.otp().strip();
    var entity = livenessRegistrationRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Liveness Registration Request Not Found"));
    if (entity.getStatus() == 2)
      throw new BadRequestException("You have already been verified");
    if (entity.getHashedOtp() == null || entity.getHashedOtp().isBlank())
      throw new BadRequestException("OTP Has Not Been Sent");
    if (!bCryptEncoder.matches(otp, entity.getHashedOtp()))
      throw new BadRequestException("Your OTP Do Not Match");
    if (LocalDateTime.now().isAfter(entity.getOtpValidUntil()))
      throw new BadRequestException("Your OTP Has Expired");

    var pensioner = pensionerRegistrationService.getByPensionSubPNo(entity.getPensionNumber());
    pensioner.setVirtualPresenceStatus(1);
    if (pensioner.getPensionPhoneNo() == null || !pensioner.getPensionPhoneNo().equals(entity.getMobileNumber()))
      pensioner.setPensionPhoneNo(entity.getMobileNumber());

    pensionerRegistrationService.registerPensionerAsUser(pensioner);
    pensionerRegistrationRepository.save(pensioner);

    entity.setStatus(2); // registered
    livenessRegistrationRequestRepository.save(entity);
  }

  public List<RoleResponseDto> getUserRoles(RoleRequestDto dto) {
    var user = userRepository.getUserByUsername(dto.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("Invalid Username or Password !"));
    if (user.getStatus() != 1)
      throw new BadRequestException("This User Is Not Active");

    if (!bCryptEncoder.matches(dto.getPassword(), user.getPasswordHash()))
      throw new BadRequestException("Invalid Password or Username !");

    return userRepository.getUserRoles(user.getId());
  }

  public String thirdPartyLogin(RoleRequestDto dto) {
    var user = userRepository.getUserByUsername(dto.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("Invalid Username or Password !"));
    if (user.getStatus() != 1)
      throw new BadRequestException("This User Is Not Active");

    if (!bCryptEncoder.matches(dto.getPassword(), user.getPasswordHash()))
      throw new BadRequestException("Invalid Password or Username !");

    var roles = userRepository.getUserRoles(user.getId());
    if (roles.isEmpty())
      throw new BadRequestException("You have not been provided with any roles");

    var targetRole = roles.get(0);
    var allowedRoles = List.of("NAGARIK_APP");
    if (!allowedRoles.contains(targetRole.getRoleNameEng()))
      throw new BadRequestException("Third Party Login Is Not Allowed For This Role");

    return jwtTokenUtil.generateAccessTokenForThirdPartyIntegration(user, targetRole);
  }

  public LoginResponseDto login(LoginRequestDto loginRequest) {
    var user = userRepository.getUserByUsername(loginRequest.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("Invalid Username or Password !"));
    if (user.getStatus() != 1)
      throw new BadRequestException("This User Is Not Active");

    if (bCryptEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
      var userRoles = userRepository.getUserRoles(user.getId());
      var targetRoles = userRoles.stream()
          .filter(
              x -> x.getRoleId() == loginRequest.getRoleId() && x.getOfficeCode().equals(loginRequest.getOfficeCode()))
          .toList();
      if (targetRoles.isEmpty())
        throw new BadRequestException("You are not privileged with this role");
      var targetRole = targetRoles.get(0);

      var disabledRoles = List.of("NAGARIK_APP");
      if (disabledRoles.contains(targetRole.getRoleNameEng()))
        throw new BadRequestException("Login Not Allowed");

      var deviceOptional = userDeviceRepository.findUserDeviceByFingerprint(user.getId(),
          loginRequest.getDeviceFingerprint());

      if (deviceOptional.isPresent()) {
        var device = deviceOptional.get();
        if (device.isTrusted()) {
          // CASE: user device is present and trusted, we send jwt (no OTP sent)
          updateUserDevice(device, user, loginRequest);
          return generateLoginResponse(user, targetRole.getRoleNameEng(), loginRequest.getOfficeCode(),
              loginRequest.getFiscalYear(), loginRequest.getDeviceFingerprint());
        } else {
          // CASE: user device is present but not trusted, we send OTP
          updateUserDevice(device, user, loginRequest);
          return createOtpResponse(user, loginRequest.getDeviceFingerprint());
        }
      } else {
        // CASE: user device not present, we register new device and send OTP
        saveNewUserDevice(user, loginRequest);
        return createOtpResponse(user, loginRequest.getDeviceFingerprint());
      }
    } else {
      throw new BadRequestException("Invalid Password or Username !");
    }
  }

  public LoginResponseDto verifyOtpAndLogin(OtpVerificationDto otpRequest) {
    User user = userRepository.getUserByUsername(otpRequest.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    var userRoles = userRepository.getUserRoles(user.getId());
    var targetRoles = userRoles.stream()
        .filter(x -> x.getRoleId() == otpRequest.getRoleId() && x.getOfficeCode().equals(otpRequest.getOfficeCode()))
        .toList();
    if (targetRoles.isEmpty())
      throw new BadRequestException("You are not privileged with this role");
    var targetRole = targetRoles.get(0);

    String deviceFingerprint = otpRequest.getDeviceFingerprint();
    String otpType = "DEVICE_VERIFICATION"; // Must match the type used when creating the OTP

    var otpDetail = otpVerificationRepository.getLatestOtpForUser(user.getId(), deviceFingerprint, otpType)
        .orElseThrow(() -> new ResourceNotFoundException("No OTP Information Found. Please login"));

    if (LocalDateTime.now().isAfter(otpDetail.getExpiryDate()))
      throw new BadRequestException("Your OTP has been expired. Please login again");
    if (otpDetail.getAttempts() >= otpDetail.getMaxAttempts())
      throw new BadRequestException("OTP verification attempts reached it's maxed limit");

    if (!otpDetail.getOtpCode().equals(otpRequest.getOtpCode())) {
      // increment the attempt counter on the specific OTP record.
      otpDetail.setAttempts(otpDetail.getAttempts() + 1);
      if (otpDetail.getAttempts() >= otpDetail.getMaxAttempts())
        otpDetail.setStatus(0); // deactivate

      otpVerificationRepository.save(otpDetail);
      int remaining = otpDetail.getMaxAttempts() - otpDetail.getAttempts();
      throw new BadRequestException("Invalid OTP: Remaining Attempts: " + remaining);
    }

    otpDetail.setVerified(true);
    otpVerificationRepository.save(otpDetail);

    markDeviceAsTrusted(user, deviceFingerprint);

    return generateLoginResponse(user, targetRole.getRoleNameEng(), otpRequest.getOfficeCode(),
        otpRequest.getFiscalYear(), deviceFingerprint);
  }

  private LoginResponseDto generateLoginResponse(User user, String role, String officeCode, String fy,
      String deviceFp) {
    var accessToken = jwtTokenUtil.generateAccessToken(user, role, officeCode, fy, deviceFp);
    var refreshToken = jwtTokenUtil.generateRefreshToken(user, role, officeCode, fy, deviceFp);

    LoginResponseDto response = new LoginResponseDto();
    response.setMessage("Login successful");
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);
    response.setNewDevice(false);

    return response;
  }

  private void updateUserDevice(UserDevice device, User user, LoginRequestDto loginRequest) {
    device.setLastUsed(LocalDateTime.now());
    device.setDeviceName(loginRequest.getDeviceName());
    device.setBrowserInfo(loginRequest.getBrowserInfo());
    device.setIpAddress(loginRequest.getIpAddress());
    device.setModifiedBy(user.getUsername());
    userDeviceRepository.save(device);
  }

  private void saveNewUserDevice(User user, LoginRequestDto loginRequest) {
    var newDevice = new UserDevice();
    newDevice.setUser(user);
    newDevice.setDeviceFingerprint(loginRequest.getDeviceFingerprint());
    newDevice.setDeviceName(loginRequest.getDeviceName());
    newDevice.setBrowserInfo(loginRequest.getBrowserInfo());
    newDevice.setIpAddress(loginRequest.getIpAddress());
    // newDevice.setRefreshToken(refreshToken);
    newDevice.setRefreshTokenExpiry(LocalDateTime.now().plusDays(7));
    newDevice.setTrusted(false); // Not trusted by default until OTP verification
    newDevice.setLastUsed(LocalDateTime.now());
    newDevice.setCreatedBy(user.getUsername());

    userDeviceRepository.save(newDevice);
  }

  private void markDeviceAsTrusted(User user, String deviceFingerprint) {
    var device = userDeviceRepository.findByUserAndDeviceFingerprint(user, deviceFingerprint)
        .orElseThrow(() -> new ResourceNotFoundException("Device Info Not Found"));

    device.setTrusted(true);
    device.setLastUsed(LocalDateTime.now());
    device.setModifiedBy(user.getUsername());
    userDeviceRepository.save(device);
  }

  private LoginResponseDto createOtpResponse(User user, String deviceFingerprint) {
    var otpCode = generateOtp();
    var message = String.format(
        "Use %s as device verification code for Pension System. Valid For 5 Minutes. Do Not Share This Code.",
        otpCode);

    if (user.getMessageMedium() == 1 && user.getMobileNo() != null) {
      smsService.sendOtpMessage(user.getMobileNo(), message);
    } else if (user.getMessageMedium() == 2 && user.getEmail() != null) {
      emailService.sendEmail(user.getEmail(), "Device Verification", message);
    } else {
      throw new BadRequestException("Couldn't Send SMS/Email Message To This User");
    }

    saveOtpVerification(user, otpCode, deviceFingerprint, "DEVICE_VERIFICATION");

    var response = new LoginResponseDto();
    response.setMessage("A verification code has been sent to your phone");
    response.setNewDevice(true);
    return response;
  }

  private String generateOtp() {
    return String.format("%06d", new Random().nextInt(1000000));
  }

  private void saveOtpVerification(User user, String otpCode, String deviceFingerprint, String otpType) {
    // disable previous opt before saving new
    otpVerificationRepository.disableActiveOtp(user.getId());

    var otpVerification = new OtpVerification();
    otpVerification.setUser(user);
    otpVerification.setOtpCode(otpCode);
    otpVerification.setDeviceFingerprint(deviceFingerprint);
    otpVerification.setOtpType(otpType);
    otpVerification.setExpiryDate(LocalDateTime.now().plusMinutes(5));
    otpVerification.setCreatedBy(user.getUsername());
    otpVerificationRepository.save(otpVerification);
  }
}