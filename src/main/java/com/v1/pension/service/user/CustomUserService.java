package com.v1.pension.service.user;

import com.v1.pension.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserService implements UserDetailsService {
  @Autowired
  UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user = userRepository.getUserByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

    // user can have multiple roles and they select their role upon login. so
    // we are passing empty GrantAuthority
    Set<GrantedAuthority> ga = new HashSet<>();
    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPasswordHash(), ga
    );
  }
}
