package com.v1.pension.service.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.v1.pension.composite_keys.UserRoleMappingId;
import com.v1.pension.dto.auth.ChangePasswordRequestDto;
import com.v1.pension.dto.auth.ResetPasswordDto;
import com.v1.pension.dto.user.BankUserRequestDto;
import com.v1.pension.dto.user.RoleMapRequestDto;
import com.v1.pension.dto.user.UserRequestDto;
import com.v1.pension.dto.user.UserResponseDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.user.User;
import com.v1.pension.model.user.UserRoleMapping;
import com.v1.pension.repository.system.OfficeRepository;
import com.v1.pension.repository.user.UserGroupRepository;
import com.v1.pension.repository.user.UserPostRepository;
import com.v1.pension.repository.user.UserRepository;
import com.v1.pension.repository.user.UserRoleMappingRepository;
import com.v1.pension.repository.user.UserRoleRepository;
import com.v1.pension.service.other.EmailService;
import com.v1.pension.service.other.SmsService;
import com.v1.pension.utils.CurrentUser;
import com.v1.pension.utils.MyResponse;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepository;
  private final OfficeRepository officeRepository;
  private final UserRoleMappingRepository userRoleMappingRepository;
  private final UserRoleRepository userRoleRepository;
  private final UserGroupRepository userGroupRepository;
  private final UserPostRepository userPostRepository;
  private final SmsService smsService;
  private final EmailService emailService;
  private final BCryptPasswordEncoder bCryptEncoder;
  private final ModelMapper modelMapper;

  private static final String USER_NOT_FOUND = "User Not Found";

  public List<Tuple> getAllUserDetails(Integer officeType) {
    if (officeType == null && CurrentUser.getUserRole().equals("BANK_ADMIN")) {
      var office = officeRepository.findByOfficeCode(CurrentUser.getOfficeCode())
          .orElseThrow(() -> new ResourceNotFoundException("Office Not Found"));
      officeType = office.getType().getId();
    } else if (officeType == null) {
      officeType = -1;
    }
    return userRepository.getAllUserDetails(officeType);
  }

  public Tuple getUserDetails() {
    return userRepository.getUserDetails(CurrentUser.getUserId(), CurrentUser.getOfficeCode(),
        CurrentUser.getUserRole(), CurrentUser.getFiscalYear());
  }

  public List<Tuple> getActiveUserByRole(String roleName) {
    var role = userRoleRepository.getRoleByName(roleName)
        .orElseThrow(() -> new ResourceNotFoundException("Role Not Found"));
    return userRepository.getActiveUserByRoleForOffice(role.getId(), CurrentUser.getOfficeCode());
  }

  public UserResponseDto getUserResponseById(long id) {
    return userRepository.getUserResponseById(id)
        .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
  }

  public List<Tuple> getAssignedRolesForUser(long userId) {
    return userRepository.getUserRolesDetails(userId);
  }

  private User getUserById(long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
  }

  @PreAuthorize("hasAuthority('BANK_ADMIN')")
  public void addBankUser(BankUserRequestDto dto) {
    var creatorOffice = officeRepository.findByOfficeCode(CurrentUser.getOfficeCode())
        .orElseThrow(() -> new ResourceNotFoundException("Office Not Found"));

    var userOffice = officeRepository.findByOfficeCode(dto.getOfficeCode())
        .orElseThrow(() -> new ResourceNotFoundException("Office Not Found"));

    if (creatorOffice.getType().getId() != userOffice.getType().getId())
      throw new BadRequestException("Selected Branch Doesn't Belong to this bank");

    var username = dto.getMobileNo().strip();
    if (userRepository.existsByUsername(username))
      throw new BadRequestException("This username already exists");

    var entity = modelMapper.map(dto, User.class);
    entity.setUsername(username);
    entity.setMobileNo(username);
    entity.setStatus(1);
    entity.setGroup(userGroupRepository.getReferenceById(2));
    entity.setPost(userPostRepository.getReferenceById(8));
    entity.setValidUpto("");
    var userPass = generatePassword();
    entity.setPasswordHash(bCryptEncoder.encode(userPass));
    userRepository.save(entity);

    // providing one default role
    var defaultRoleMap = new UserRoleMapping();
    defaultRoleMap.setUser(entity);
    defaultRoleMap.setOfficeCode(dto.getOfficeCode());
    defaultRoleMap.setRole(userRoleRepository.getReferenceById(5));
    userRoleMappingRepository.save(defaultRoleMap);

    var message = "Welcome to Pension Management System, Username : " + entity.getUsername() + " and Password: "
        + userPass;
    if (entity.getMessageMedium() == 1 && entity.getMobileNo() != null) {
      smsService.sendAlertMessage(entity.getMobileNo(), message);
    } else if (entity.getMessageMedium() == 2 && entity.getEmail() != null) {
      emailService.sendEmail(entity.getEmail(), "User Registration Success", message);
    } else {
      throw new BadRequestException("Couldn't Send SMS/Email Message To This User");
    }
  }

  public void addUser(UserRequestDto dto) {
    var officeInfo = officeRepository.findByOfficeCode(dto.getOfficeCode());
    if (officeInfo.isEmpty())
      throw new BadRequestException("Office Not Found");

    if (userRepository.existsByUsername(dto.getUsername()))
      throw new BadRequestException("This username already exists");

    var entity = modelMapper.map(dto, User.class);
    entity.setUsername(entity.getUsername().strip());
    entity.setGroup(userGroupRepository.getReferenceById(dto.getUserGroupId()));
    entity.setPost(userPostRepository.getReferenceById(dto.getPostId()));
    entity.setValidUpto("");
    var userPass = generatePassword();
    entity.setPasswordHash(bCryptEncoder.encode(userPass));
    userRepository.save(entity);

    // providing one default role
    var defaultRoleMap = new UserRoleMapping();
    defaultRoleMap.setUser(entity);
    defaultRoleMap.setOfficeCode(entity.getOfficeCode());
    defaultRoleMap.setRole(userRoleRepository.getReferenceById(dto.getRoleId()));
    userRoleMappingRepository.save(defaultRoleMap);

    var message = "Welcome to Pension Management System, Username : " + entity.getUsername() + " and Password: "
        + userPass;
    if (entity.getMessageMedium() == 1 && entity.getMobileNo() != null) {
      smsService.sendAlertMessage(entity.getMobileNo(), message);
    } else if (entity.getMessageMedium() == 2 && entity.getEmail() != null) {
      emailService.sendEmail(entity.getEmail(), "User Registration Success", message);
    } else {
      throw new BadRequestException("Couldn't Send SMS/Email Message To This User");
    }
  }

  public void updateUser(long id, UserRequestDto request) {
    var entity = getUserById(id);
    entity.setFullName(request.getFullName());
    entity.setMobileNo(request.getMobileNo());
    entity.setEmail(request.getEmail());
    entity.setMessageMedium(request.getMessageMedium());
    entity.setStatus(request.getStatus());
    entity.setPost(userPostRepository.getReferenceById(request.getPostId()));
    userRepository.save(entity);
  }

  public void updateUserRoleMapping(RoleMapRequestDto dto) {
    var userRoleMapId = new UserRoleMappingId(dto.getUserId(), dto.getRoleId(), dto.getOfficeCode());
    var roleMapDup = userRoleMappingRepository.findById(userRoleMapId);
    if (roleMapDup.isPresent()) {
      var roleMap = roleMapDup.get();
      roleMap.setStatus(dto.getStatus());
      userRoleMappingRepository.save(roleMap);
    } else {
      var roleMap = new UserRoleMapping();
      roleMap.setUser(userRepository.getReferenceById(dto.getUserId()));
      roleMap.setRole(userRoleRepository.getReferenceById(dto.getRoleId()));
      roleMap.setOfficeCode(dto.getOfficeCode());
      roleMap.setStatus(dto.getStatus());
      userRoleMappingRepository.save(roleMap);
    }
  }

  public void changePassword(ChangePasswordRequestDto request) {
    var user = userRepository.getUserByUsername(CurrentUser.getUserName())
        .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));

    if (!bCryptEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
      throw new BadRequestException("Current password is incorrect");
    if (!request.getNewPassword().equals(request.getConfirmPassword()))
      throw new BadRequestException("New password and confirm password do not match");

    user.setPasswordHash(bCryptEncoder.encode(request.getNewPassword()));
    userRepository.save(user);
  }

  public void resetPassword(ResetPasswordDto dto) {
    var userDup = userRepository.getUserByUsername(dto.getUserName());
    if (userDup.isEmpty())
      throw new BadRequestException(USER_NOT_FOUND);
    var user = userDup.get();

    if (dto.getMessageMedium() == 1 && !user.getMobileNo().equals(dto.getMobileOrEmail()))
      throw new BadRequestException("User Not Found With This Username And Mobile");

    if (dto.getMessageMedium() == 2 && !user.getEmail().equals(dto.getMobileOrEmail()))
      throw new BadRequestException("User Not Found With This Username And Email");

    var userNewPass = generatePassword();
    user.setPasswordHash(bCryptEncoder.encode(userNewPass));
    userRepository.save(user);

    var message = "Pension System: Your Password Has Been Reset. Username : " + user.getUsername()
        + " and New Password: " + userNewPass;
    if (dto.getMessageMedium() == 1 && user.getMobileNo() != null) {
      smsService.sendAlertMessage(user.getMobileNo(), message);
    } else if (dto.getMessageMedium() == 2 && user.getEmail() != null) {
      emailService.sendEmail(user.getEmail(), "Password Reset Success", message);
    } else {
      throw new BadRequestException("Couldn't Send SMS/Email Message To This User");
    }
  }

  private String generatePassword() {
    return "ipms@" + new Random().nextInt(1000000);
  }

  public Map<String, Object> getDashboardInfo() {
    var roleName = CurrentUser.getUserRole();

    var taskInfo = userRepository.getDashboardTaskInfo(roleName, CurrentUser.getOfficeCode());
    var categoryBreakdown = userRepository.getDashboardCategoryBreakdown();
    var provinceData = userRepository.getDashboardProvinceData();
    var kpiData = userRepository.getDashboardKpiData(CurrentUser.getFiscalYear());
    var pensionerData = userRepository.getDashboardPensionerData();

    Map<String, Object> data = new HashMap<>();
    data.put("taskInfo", MyResponse.data(taskInfo));
    data.put("categories", MyResponse.data(categoryBreakdown));
    data.put("provinceData", MyResponse.data(provinceData));
    data.put("kpiData", MyResponse.data(kpiData));
    data.put("pensionerData", MyResponse.data(pensionerData));

    return data;
  }
}
