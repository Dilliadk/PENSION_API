package com.v1.pension.service.other;

import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.system.OfficeRepository;
import com.v1.pension.repository.user.UserRoleRepository;
import com.v1.pension.utils.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("customSecurityService")
@RequiredArgsConstructor
public class CustomSecurityService {
  private final UserRoleRepository userRoleRepository;
  private final OfficeRepository officeRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;

  public boolean hasPermissionOverPensioner(String subPNo) {
    var userGroupId = userRoleRepository.getUserGroupIdForRole(CurrentUser.getUserRole());
    var districtCode = officeRepository.getDistrictCodeForOffice(CurrentUser.getOfficeCode());
    return pensionerRegistrationRepository.hasPermissionOverPensioner(subPNo, userGroupId, districtCode)
        .isPresent();
  }
}
