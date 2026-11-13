package com.v1.pension.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.repository.system.BankRepository;
import com.v1.pension.repository.system.DistrictRepository;
import com.v1.pension.repository.system.DocumentTypeRepository;
import com.v1.pension.repository.system.FiscalYearRepository;
import com.v1.pension.repository.system.GenderRepository;
import com.v1.pension.repository.system.NepaliCalendarRepository;
import com.v1.pension.repository.system.PalikaRepository;
import com.v1.pension.repository.system.PensionCalculationTypeRepository;
import com.v1.pension.repository.system.PensionerCategoryRepository;
import com.v1.pension.repository.system.PensionerOtherTypeRepository;
import com.v1.pension.repository.system.PensionerRelationRepository;
import com.v1.pension.repository.system.PensionerStatusRepository;
import com.v1.pension.repository.system.PensionerTypeRepository;
import com.v1.pension.repository.system.ProvinceRepository;
import com.v1.pension.repository.system.StatusRepository;
import com.v1.pension.repository.user.UserGroupRepository;
import com.v1.pension.repository.user.UserRoleRepository;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {
  private final ProvinceRepository provinceRepository;
  private final DistrictRepository districtRepository;
  private final PalikaRepository palikaRepository;
  private final StatusRepository statusRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserRoleRepository userRoleRepository;
  private final PensionerTypeRepository pensionerTypeRepository;
  private final PensionerCategoryRepository pensionerCategoryRepository;
  private final GenderRepository genderRepository;
  private final PensionerRelationRepository relationsRepository;
  private final PensionerStatusRepository pensionerStatusRepository;
  private final PensionerOtherTypeRepository pensionerOtherTypeRepository;
  private final PensionCalculationTypeRepository calculationTypeRepository;
  private final DocumentTypeRepository documentTypeRepository;
  private final NepaliCalendarRepository nepaliCalendarRepository;
  private final FiscalYearRepository fiscalYearRepository;
  private final BankRepository bankRepository;

  @GetMapping("/current-date")
  public ResponseEntity<Map<String, Object>> getCurrentDate() {
    Map<String, Object> data = new HashMap<>();
    var localDate = LocalDate.now();
    var formattedDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    data.put("currentDate", formattedDate);
    return ResponseEntity.ok(data);
  }

  @GetMapping("/fiscal-years")
  public ResponseEntity<?> getFiscalYears() {
    var data = fiscalYearRepository.getActiveFiscalYears();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/province/all")
  public ResponseEntity<?> getAllProvince() {
    var data = provinceRepository.getAllProvinces();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/district/all")
  public ResponseEntity<?> getAllDistricts() {
    var data = districtRepository.getAllDistricts();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/district/filter")
  public ResponseEntity<?> getFilteredDistricts(@RequestParam String provinceCode) {
    var data = districtRepository.getFilteredDistricts(provinceCode);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/palika/filter")
  public ResponseEntity<?> getFilteredPalikaList(@RequestParam String districtCode) {
    var data = palikaRepository.getPalikaByDistrict(districtCode);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/status/all")
  public ResponseEntity<?> getAllStatus() {
    var data = statusRepository.getStatusList();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/user-groups/active")
  public ResponseEntity<?> getActiveUserGroups() {
    var data = userGroupRepository.getActiveUserGroups();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/roles/active")
  public ResponseEntity<?> getActiveUserRoles(@RequestParam(required = false, defaultValue = "-1") int groupId) {
    var data = userRoleRepository.getActiveRoles(groupId);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-types/active")
  public ResponseEntity<?> getActivePensionerTypes() {
    var data = pensionerTypeRepository.getActivePensionerTypeSummaries();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-types/all")
  public ResponseEntity<?> getAllPensionerTypes() {
    var data = pensionerTypeRepository.getAllPensionerTypeSummaries();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-types/payable")
  public ResponseEntity<?> getPayablePensionerTypes() {
    var data = pensionerTypeRepository.getPayablePensionerTypes();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-relation/active")
  public ResponseEntity<?> getActivePensionerRelation() {
    var data = relationsRepository.getActivePensionerRelationSummaries();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-relation/all")
  public ResponseEntity<?> getAllPensionerRelation() {
    var data = relationsRepository.getAllPensionerRelation();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-status/active")
  public ResponseEntity<?> getActivePensionerStatus() {
    var data = pensionerStatusRepository.getActivePensionerStatusSummaries();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-status/all")
  public ResponseEntity<?> getAllPensionerStatus() {
    var data = pensionerStatusRepository.getAllPensionerStatus();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-other-type/all")
  public ResponseEntity<?> getAllPensionerOtherTYpe() {
    var data = pensionerOtherTypeRepository.getAllPensionerOtherType();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-other-type/active")
  public ResponseEntity<?> getActivePensionerOtherType() {
    var data = pensionerOtherTypeRepository.getActivePensionerOtherTypeSummaries();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pension-calculation-type/active")
  public ResponseEntity<?> getActivePensionerCalculationType() {
    var data = calculationTypeRepository.getActivePensionCalculationTypeSummaries();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pension-calculation-type/all")
  public ResponseEntity<?> getAllPensionerCalculationType() {
    var data = calculationTypeRepository.getAllPensionCalculationType();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-categories/active-by-type")
  public ResponseEntity<?> getActivePensionerCategoriesByType(@RequestParam String ptCode) {
    var data = pensionerCategoryRepository.getActivePensionerCategoriesByType(ptCode);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/pensioner-categories/all")
  public ResponseEntity<?> getAllPensionerCategories() {
    var data = pensionerCategoryRepository.getAllPensionerCategories();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/genders/all")
  public ResponseEntity<?> getAllGenders() {
    var data = genderRepository.getAllGenders();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/document-types/active")
  public ResponseEntity<?> getActiveDocumentTypes() {
    var data = documentTypeRepository.getActiveDocumentTypes();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/monthly-last-date")
  public ResponseEntity<?> getMonthlyLastData(@RequestParam String date) {
    var dateParts = Arrays.stream(date.split("-")).toList();
    var year = Integer.parseInt(dateParts.get(0));
    var month = Integer.parseInt(dateParts.get(1));

    var dateInfo = nepaliCalendarRepository.findByYearAndMonthAndDay(year, month, 1)
        .orElseThrow(() -> new ResourceNotFoundException("Nepali Date Record Not Found In Database"));
    var lastDate = String.format("%d-%02d-%02d", dateInfo.getNepaliYear(), dateInfo.getNepaliMonth(),
        dateInfo.getNepaliDaysInMonth());
    return ResponseEntity.ok(Map.of("lastDate", lastDate));
  }

  @GetMapping("/banks/active")
  public ResponseEntity<?> getActiveBanks() {
    var data = bankRepository.getActiveBanks();
    return ResponseEntity.ok(MyResponse.data(data));
  }
}
