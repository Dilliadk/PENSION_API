package com.v1.pension.service.pensioner;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;

import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.pensioner.PensionerPresence;
import com.v1.pension.repository.pensioner.PensionPresenceRepository;
import com.v1.pension.repository.pensioner.PensionerRegistrationRepository;
import com.v1.pension.repository.system.OfficeRepository;
import com.v1.pension.repository.user.UserRoleRepository;
import com.v1.pension.utils.CurrentUser;

@Service
@RequiredArgsConstructor
public class PensionPresenceService {
  private final PensionPresenceRepository pensionPresenceRepository;
  private final PensionerRegistrationRepository pensionerRegistrationRepository;
  private final UserRoleRepository userRoleRepository;
  private final OfficeRepository officeRepository;

  public List<Tuple> getAllBySubPNo(String subPNo, int type) {
    return pensionPresenceRepository.getAllBySubPNo(subPNo);
  }

  public PensionerPresence getById(long id) {
    return pensionPresenceRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Presence Data Not Found"));
  }

  public void savePensionerPresence(PensionerPresence request) throws IOException {
    var pensioner = pensionerRegistrationRepository.findByPensionSubPNo(request.getPensionSubPNo())
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Not Found"));

    var userGroupId = userRoleRepository.getUserGroupIdForRole(CurrentUser.getUserRole());
    var userOffice = officeRepository.findByOfficeCode(CurrentUser.getOfficeCode())
        .orElseThrow(() -> new ResourceNotFoundException("Office Not Found"));

    if (userGroupId == 4) {
      throw new BadRequestException("Access Denied");
    } else if (userGroupId == 2) {
      // for banks
      var officeType = userOffice.getType();
      if (pensioner.getMainBankCode() != officeType.getId())
        throw new BadRequestException("This pensioner does not have account in " + officeType.getDescription());
    } else if (userGroupId == 3) {
      // for DTCO
      var hasPermission = pensionerRegistrationRepository.hasPermissionOverPensioner(request.getPensionSubPNo(),
          userGroupId, userOffice.getDistrictCode());
      if (hasPermission.isEmpty())
        throw new BadRequestException("This pensioner has bank account in different district");
    }

    var file = request.getFile();
    if (file != null) {
      request.setFileData(file.getBytes());
      request.setContentType(file.getContentType());
    } else {
      if (request.getId() != null) {
        var oldEntity = pensionPresenceRepository.findById(request.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Pensioner Presence Not Found"));
        request.setFileData(oldEntity.getFileData());
        request.setContentType(oldEntity.getContentType());
      }
    }

    if (pensioner.getPensionStatus() == 2) {
      pensioner.setPensionStatus(1); // active inactive pensioner
    }

    pensionPresenceRepository.save(request);
  }

  public Optional<PensionerPresence> getPensionPresenceById(long id) {
    return pensionPresenceRepository.findById(id);
  }
}
