package com.v1.pension.controller.user;

import java.util.List;
import lombok.RequiredArgsConstructor;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.dto.auth.ChangePasswordRequestDto;
import com.v1.pension.dto.auth.ResetPasswordDto;
import com.v1.pension.dto.user.BankUserRequestDto;
import com.v1.pension.dto.user.RoleMapRequestDto;
import com.v1.pension.dto.user.UserRequestDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.repository.user.LivenessRegistrationRequestRepository;
import com.v1.pension.service.user.UserService;
import com.v1.pension.utils.CurrentUser;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final LivenessRegistrationRequestRepository livenessRegistrationRequestRepository;

  @GetMapping("/all/details")
  public ResponseEntity<?> getAllUserDetails(@RequestParam(required = false) Integer officeType) {
    var data = userService.getAllUserDetails(officeType);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/my-details")
  public ResponseEntity<?> getUserDetails() {
    var data = userService.getUserDetails();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/active/by-role")
  public ResponseEntity<?> getActiveUserByRole(@RequestParam String role) {
    var data = userService.getActiveUserByRole(role);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(@PathVariable long id) {
    var data = userService.getUserResponseById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> addUser(@Valid @RequestBody UserRequestDto dto) {
    userService.addUser(dto);
    return ResponseEntity.ok(MyResponse.message("User has been saved"));
  }

  @PostMapping("/bank-users")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> addBankUser(@Valid @RequestBody BankUserRequestDto dto) {
    userService.addBankUser(dto);
    return ResponseEntity.ok(MyResponse.message("User has been saved"));
  }

  @PutMapping("/{id}")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<?> updateUser(@PathVariable long id, @Valid @RequestBody UserRequestDto dto) {
    userService.updateUser(id, dto);
    return ResponseEntity.ok(MyResponse.message("User has been updated"));
  }

  @GetMapping("/{id}/assigned-roles")
  public ResponseEntity<?> getAssignedRoles(@PathVariable long id) {
    var data = userService.getAssignedRolesForUser(id);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @PostMapping("/update-user-role")
  public ResponseEntity<?> updateUserRoleMapping(@Valid @RequestBody RoleMapRequestDto dto) {
    userService.updateUserRoleMapping(dto);
    return ResponseEntity.ok(MyResponse.message("User role has been updated"));
  }

  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequestDto request) {
    var disabledUsers = List.of("nagarik_app");
    if (disabledUsers.contains(CurrentUser.getUserName()))
      throw new BadRequestException("Change Password Not Allowed");

    userService.changePassword(request);
    return ResponseEntity.ok(MyResponse.message("Password changed successfully"));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
    var disabledUsers = List.of("nagarik_app");
    if (disabledUsers.contains(dto.getUserName()))
      throw new BadRequestException("Reset Password Not Allowed");

    userService.resetPassword(dto);
    return ResponseEntity.ok(MyResponse.message("Password Reset Success"));
  }

  @GetMapping("/dashboard-info")
  public ResponseEntity<?> getDashboardInfo() {
    var data = userService.getDashboardInfo();
    return ResponseEntity.ok(data);
  }

  @GetMapping("/liveness-registrations")
  public ResponseEntity<Object> getLivenessRegistrations() {
    var data = livenessRegistrationRequestRepository.getLivenessRegistrationList();
    return ResponseEntity.ok(MyResponse.data(data));
  }
}
