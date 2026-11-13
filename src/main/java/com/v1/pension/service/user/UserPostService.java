package com.v1.pension.service.user;

import java.util.List;
import lombok.RequiredArgsConstructor;

import jakarta.persistence.Tuple;

import org.springframework.stereotype.Service;

import com.v1.pension.repository.user.UserPostRepository;

@Service
@RequiredArgsConstructor
public class UserPostService {
  private final UserPostRepository postRepository;

  public List<Tuple> getActiveUserPosts() {
    return postRepository.getActiveUserPosts();
  }
}
