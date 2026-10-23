package com.devtrack.auth;

import com.devtrack.common.UserPrincipal;
import com.devtrack.user.UserAccount;
import com.devtrack.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
  private final UserRepository users;

  public DatabaseUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    UserAccount user = users.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("Account not found"));
    return new UserPrincipal(user.getId(), user.getEmail(), user.getPasswordHash());
  }
}
