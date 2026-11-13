package com.v1.pension.controller.system;

import com.v1.pension.dto.user.RoleMenuRequestDto;
import com.v1.pension.service.system.MenuService;
import com.v1.pension.utils.MyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {
  private final MenuService menuService;

  @GetMapping("/main-menu/all")
  public ResponseEntity<Object> getAllMainMenu() {
    var data = menuService.getAllMenuMain();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/sub-menu/filter-by-main")
  public ResponseEntity<Object> getAllSubMenuByMain(@RequestParam long menuMainId) {
    var data = menuService.getAllMenuSubByMain(menuMainId);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/button-menu/for-role")
  public ResponseEntity<Object> getMenuButtonDetailsForRole(@RequestParam long roleId, @RequestParam long menuSubId) {
    var data = menuService.getMenuButtonDetailsForRole(roleId, menuSubId);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping(value = "/full-menu-details", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> getMyMenus() {
    return ResponseEntity.ok(menuService.getMyFullMenuDetails());
  }

  @PostMapping("/role-menu-mapping")
  @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
  public ResponseEntity<Object> addMenusForRole(@Valid @RequestBody RoleMenuRequestDto dto) {
    menuService.addMenusForRole(dto);
    return ResponseEntity.ok(MyResponse.message("Menu Mapping Saved"));
  }
}
