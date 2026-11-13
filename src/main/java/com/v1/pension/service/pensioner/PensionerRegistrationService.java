package com.v1.pension.service.pensioner;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.v1.pension.dto.pis.PisPensionerMainDataResponse;
import com.v1.pension.dto.pis.PisPensionerPersonInfo;
import com.v1.pension.dto.tp_integration.NagarikAppPensionerResponse;
import com.v1.pension.dto.user.UserRequestDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.PensionerRegistration;
import com.v1.pension.repository.pensioner.PensionPresenceRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.system.OfficeRepository;
import com.v1.pension.repository.user.UserRepository;
import com.v1.pension.repository.user.UserRoleRepository;
import com.v1.pension.service.other.PisService;
import com.v1.pension.service.user.UserService;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class PensionerRegistrationService {
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final PisService pisService;
  private final UserService userService;
  private final UserRepository userRepository;
  private final UserRoleRepository userRoleRepository;
  private final OfficeRepository officeRepository;
  private final PensionPresenceRepository presenceRepository;

  private static final String SERVICE_RECEIVER_CODE = "service_receiver_code";
  private static final String SERVICE_RECEIVER_VALUE = "P@nS!O$753";

  public List<Tuple> getActivePensioners() {
    return pensionerRegistrationRepository.getActivePensioners();
  }

  public PensionerRegistration getById(long id) {
    return pensionerRegistrationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));
  }

  // it is used to prevent DTCO from modifying pensioner belonging to banks of another district
  public void hasPermissionOverPensioner(String subPNo) {
    var userGroupId = userRoleRepository.getUserGroupIdForRole(CurrentUser.getUserRole());
    var districtCode = officeRepository.getDistrictCodeForOffice(CurrentUser.getOfficeCode());
    var dup = pensionerRegistrationRepository.hasPermissionOverPensioner(subPNo, userGroupId, districtCode);
    if (dup.isEmpty())
      throw new BadRequestException("यस पेन्सनर यस जिल्ला अन्तर्गतका बैंकमा हुनुहुन्न");
  }

  public List<Tuple> getPensionerInfoBySubPNo(String subPNo) {
    return pensionerRegistrationRepository.getPensionerInfoBySubPNo(subPNo);
  }

  public PensionerRegistration searchWithPid(String number) {
    Map<String, Object> formData = new HashMap<>();
    formData.put(SERVICE_RECEIVER_CODE, SERVICE_RECEIVER_VALUE);
    formData.put("pension_number", number);
    return searchWithPisPidNo(formData);
  }

  public PensionerRegistration searchWithPis(String number) {
    Map<String, Object> formData = new HashMap<>();
    formData.put(SERVICE_RECEIVER_CODE, SERVICE_RECEIVER_VALUE);
    formData.put("pis_number", number);
    return searchWithPisPidNo(formData);
  }

  private PensionerRegistration searchWithPisPidNo(Map<String, Object> formData) {
    var data = pisService.getPisDataForPensioner(formData).block();
    if (data == null || !data.isSuccess())
      throw new BadRequestException("Failed to fetch data from PIS software");

    var personInfo = data.getData().getPerson_info();
    if (personInfo.isEmpty())
      throw new BadRequestException("Failed to fetch personal info from PIS");

    return generateMainPensionerFromPisData(personInfo.get(0));
  }

  public PisPensionerMainDataResponse searchWithPidRaw(String number) {
    Map<String, Object> formData = new HashMap<>();
    formData.put(SERVICE_RECEIVER_CODE, SERVICE_RECEIVER_VALUE);
    formData.put("pension_number", number);
    return pisService.getPisDataForPensioner(formData).block();
  }

  public PisPensionerMainDataResponse searchWithPisRaw(String number) {
    Map<String, Object> formData = new HashMap<>();
    formData.put(SERVICE_RECEIVER_CODE, SERVICE_RECEIVER_VALUE);
    formData.put("pis_number", number);
    return pisService.getPisDataForPensioner(formData).block();
  }

  private PensionerRegistration generateMainPensionerFromPisData(PisPensionerPersonInfo data) {
    var pensioner = new PensionerRegistration();
    pensioner.setPensionNo(data.getPension_number());
    pensioner.setSanketNo(data.getPis_number());
    pensioner.setPensionNameNep(data.getFull_name_np());
    pensioner.setPensionNameEng(data.getFull_name_en());
    pensioner.setPensionDobNep(data.getDob_bs());
    pensioner.setPensionDistrict(data.getDistrict_code());
    pensioner.setVdc(data.getVdc_code());
    pensioner.setWard(data.getFed_permanent_ward_no());
    pensioner.setPensionRetireOffice(data.getOffice_name());
    pensioner.setPensionAmountPerMonth(data.getTotal_payable_amount());
    pensioner.setCitizenshipNo(data.getCitizenship_number());
    pensioner.setPensionBankCode(data.getBank_branch());
    pensioner.setPensionRemarks(data.getDescription());
    pensioner.setPensionRetireNep(data.getRetired_date_bs());
    pensioner.setStage(0);
    pensioner.setPensionStatus(1);
    pensioner.setFestivalMonth(6);
    pensioner.setIsIncrement(1);
    pensioner.setMessageMedium(1);
    pensioner.setPensionMaritalStatus(1);
    pensioner.setMainBankCode(null);
    pensioner.setPensionReceivePercentage(100);
    pensioner.setPensionIncrementPercentage(100);

    if (StringUtils.hasText(data.getMobiles()))
      pensioner.setPensionPhoneNo(data.getMobiles());
    if (StringUtils.hasText(data.getEmail()))
      pensioner.setPensionEmail(data.getEmail());

    return pensioner;
  }

  private void addDefaultNationalId(PensionerRegistration pensioner) {
    if (!pensioner.getOtherTypeCode().equals("6")
        && (pensioner.getNationalId() == null || pensioner.getNationalId().isBlank())) {
      var postFix = switch (pensioner.getOtherTypeCode()) {
        case "1" -> "1"; // ९० बर्ष र सो भन्दा माथि
        case "2" -> "3"; // भारतिय
        case "3" -> "2"; // अपाङ्ग
        case "4" -> "4"; // शैक्षिक
        case "5" -> "5"; // सन्तती
        default -> "0";
      };

      pensioner.setNationalId("999-999-999-" + postFix);
    }
  }

  private void preventPensionSubNumberUpdate(PensionerRegistration newData) {
    if (newData.getId() == null)
      return;

    var currentData = pensionerRegistrationRepository.findById(newData.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    if (!currentData.getPensionSubPNo().equals(newData.getPensionSubPNo()))
      throw new BadRequestException("Cannot update pension number");
  }

  public void saveMainPensioner(PensionerRegistration request) {
    if (request.getId() == null && pensionerRegistrationRepository.existsByPensionNo(request.getPensionNo()))
      throw new BadRequestException("Main pensioner with this pensionId already exists");

    if (request.getPensionIncrementPercentage() > 100)
      throw new BadRequestException("Increment percentage cannot be more than 100");

    request.setPensionSubPNo(request.getPensionNo());
    preventPensionSubNumberUpdate(request);
    addDefaultNationalId(request);
    pensionerRegistrationRepository.save(request);

    if (request.getVirtualPresenceStatus() == 1 && !userRepository.existsByUsername(request.getPensionSubPNo())) {
      registerPensionerAsUser(request);
    }
  }

  public void saveSubPensioner(PensionerRegistration request) {
    if (request.getId() == null && pensionerRegistrationRepository.existsByPensionSubPNo(request.getPensionSubPNo()))
      throw new BadRequestException("Sub pensioner with this Sub-Pension-No already exists");
    if (request.getPensionIncrementPercentage() > 100)
      throw new BadRequestException("Increment percentage cannot be more than 100");

    var mp = pensionerRegistrationRepository.findByPensionSubPNo(request.getPensionNo())
        .orElseThrow(() -> new ResourceNotFoundException("Main Pensioner Not Found"));
    mp.setPensionDeathDateNep(request.getPensionDeathDateNep());
    if (request.isMpDead())
      mp.setPensionStatus(6); // 6 = MP_Dead
    pensionerRegistrationRepository.save(mp);

    DecimalFormat df = new DecimalFormat("#.##");
    request.setPensionDeathDateNep("");
    request.setPensionPostCode(mp.getPensionPostCode());
    request.setMainBankCode(mp.getMainBankCode());
    request.setPensionBankCode(mp.getPensionBankCode());
    var pensionAmountPerMonth = mp.getPensionAmountPerMonth() * request.getPensionReceivePercentage() / 100;
    request.setPensionAmountPerMonth(Double.parseDouble(df.format(pensionAmountPerMonth)));
    request.setPensionNameNep(mp.getPensionNameNep());
    request.setPensionNameEng(mp.getPensionNameEng());
    request.setPensionTypeCode(mp.getPensionTypeCode());

    preventPensionSubNumberUpdate(request);
    addDefaultNationalId(request);
    pensionerRegistrationRepository.save(request);

    if (request.getVirtualPresenceStatus() == 1 && !userRepository.existsByUsername(request.getPensionSubPNo())) {
      registerPensionerAsUser(request);
    }
  }

  public void saveDirectSubPensioner(PensionerRegistration request) {
    // if (request.getId() == null && pensionerRegistrationRepository.existsByPensionNo(request.getPensionNo()))
    //   throw new BadRequestException("Direct Sub Pensioner with this pensionId already exists");

    if (request.getId() == null && pensionerRegistrationRepository.existsByPensionSubPNo(request.getPensionSubPNo()))
      throw new BadRequestException("Direct Sub Pensioner with this Sub-Pension-No already exists");
    if (request.getPensionIncrementPercentage() > 100)
      throw new BadRequestException("Increment percentage cannot be more than 100");

    request.setPensionSubPensioner(1);
    preventPensionSubNumberUpdate(request);
    addDefaultNationalId(request);
    pensionerRegistrationRepository.save(request);

    if (request.getVirtualPresenceStatus() == 1 && !userRepository.existsByUsername(request.getPensionSubPNo())) {
      registerPensionerAsUser(request);
    }
  }

  public List<Tuple> getSubPensionersByPensionNo(String pensionNo) {
    return pensionerRegistrationRepository.getSubPensionersByPensionNo(pensionNo);
  }

  public PensionerRegistration getByPensionSubPNo(String subPNo) {
    return pensionerRegistrationRepository.findByPensionSubPNo(subPNo)
        .orElseThrow(() -> new ResourceNotFoundException("यस पट्टा नं. बाट कुनै निवृत्तभरक भेटिएन"));
  }

  public List<Tuple> findByPensionNo(String pensionNo) {
    var result = pensionerRegistrationRepository.getAllMainPensionerByPensionNo(pensionNo);
    if (result == null || result.isEmpty()) {
      throw new ResourceNotFoundException("Main pensioner not found");
    }
    return result;
  }

  public List<Tuple> getDirectSubPensioner(String pensionNo) {
    return pensionerRegistrationRepository.getDirectSubPensioner(pensionNo);
  }

  public void registerPensionerAsUser(PensionerRegistration pensioner) {
    var data = new UserRequestDto();
    var name = pensioner.getPensionSubPensioner() == 0 ? pensioner.getPensionNameNep()
        : pensioner.getPensionSubNameNep();
    data.setUserGroupId(4);
    data.setRoleId(11);
    data.setUsername(pensioner.getPensionSubPNo()); // subPNo is unique
    data.setFullName(name);
    data.setMobileNo(pensioner.getPensionPhoneNo());
    data.setOfficeCode("00001"); // PMO
    data.setEmail(pensioner.getPensionEmail());
    data.setMessageMedium(pensioner.getMessageMedium());
    data.setPostId(15);
    data.setStatus(1);

    userService.addUser(data);
  }

  public NagarikAppPensionerResponse getPensionerInfoForNagarikApp(String subPNo) {
    if (!pensionerRegistrationRepository.existsByPensionSubPNo(subPNo))
      throw new BadRequestException("Pensioner Not Found");

    var info = pensionerRegistrationRepository.getPensionerInfoForNagarikApp(subPNo);
    var presenceHistory = presenceRepository.getPresenceHistoryForPensioner(subPNo);

    var res = new NagarikAppPensionerResponse();
    res.setPensionerInfo(info);
    res.setPresenceHistory(presenceHistory);
    return res;
  }

  public boolean isKycValid(String subPNo) {
    if (!pensionerRegistrationRepository.existsByPensionSubPNo(subPNo))
      throw new BadRequestException("Pensioner with this pension number not found");

    var info = pensionerRegistrationRepository.getPensionerInfoForNagarikApp(subPNo);
    return info.getKycValid();
  }
}