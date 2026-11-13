package com.v1.pension.controller.user;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.v1.pension.service.user.UserPostService;
import com.v1.pension.utils.MyResponse;

@RestController
@RequestMapping("/api/user-posts")
@RequiredArgsConstructor
public class UserPostController {
  private final UserPostService postService;

  @GetMapping("/active")
  public ResponseEntity<?> getActiveUserPosts() {
    var data = postService.getActiveUserPosts();
    return ResponseEntity.ok(MyResponse.data(data));
  }
}
