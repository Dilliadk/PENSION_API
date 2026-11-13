package com.v1.pension.service.system;

import com.v1.pension.dto.user.RoleMenuRequestDto;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.user.RoleMenuMapping;
import com.v1.pension.repository.system.MenuMainRepository;
import com.v1.pension.repository.system.MenuSubRepository;
import com.v1.pension.repository.user.RoleMenuMappingRepository;
import com.v1.pension.repository.user.UserRoleRepository;
import com.v1.pension.utils.CurrentUser;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {
  private final MenuMainRepository menuMainRepository;
  private final MenuSubRepository menuSubRepository;
  private final UserRoleRepository roleRepository;
  private final RoleMenuMappingRepository roleMenuMappingRepository;

  public List<Tuple> getAllMenuMain() {
    return menuMainRepository.getAllMenuMain();
  }

  public List<Tuple> getAllMenuSubByMain(long menuMainId) {
    return menuSubRepository.getAllMenuSubByMenuMain(menuMainId);
  }

  public List<Tuple> getMenuButtonDetailsForRole(long roleId, long menuSubId) {
    return roleMenuMappingRepository.getMenuButtonDetailsForRole(roleId, menuSubId);
  }

  public String getMyFullMenuDetails() {
    var userRole = roleRepository.getRoleByName(CurrentUser.getUserRole())
        .orElseThrow(() -> new ResourceNotFoundException("Role not found !"));
    var data = roleMenuMappingRepository.getFullMenuDetailsForRole(userRole.getId());
    if (data == null) return "[]";
    return data;
  }

  public void addMenusForRole(RoleMenuRequestDto dto) {
    var filteredMenus = new ArrayList<RoleMenuMapping>();
    for (var entity : dto.getMenu()) {
      entity.setRole(roleRepository.getReferenceById(dto.getRoleId()));
      filteredMenus.add(entity);
    }

    roleMenuMappingRepository.saveAll(filteredMenus);
  }
}
