package com.example.jammoney.auth.service;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.exception.UserNotFoundException;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(UserNotFoundException::new);
        return new CustomUserDetails(user);
    }
}
