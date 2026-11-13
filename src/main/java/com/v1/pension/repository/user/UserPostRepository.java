package com.v1.pension.repository.user;

import java.util.List;

import jakarta.persistence.Tuple;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.v1.pension.model.user.UserPost;

public interface UserPostRepository extends JpaRepository<UserPost, Integer> {
  @Query(nativeQuery = true, value = """
      select id, name_nep, name_eng
      from user_posts where status = 1
      """)
  List<Tuple> getActiveUserPosts();
}
