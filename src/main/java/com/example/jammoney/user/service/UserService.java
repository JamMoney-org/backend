package com.example.jammoney.user.service;

import com.example.jammoney.exception.EmailAlreadyExistsException;
import com.example.jammoney.exception.ErrorCode;
import com.example.jammoney.exception.PasswordMismatchException;
import com.example.jammoney.user.Role;
import com.example.jammoney.user.dto.UserRequestDto;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UserRequestDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException(ErrorCode.PASSWORD_MISMATCH);
        }

        String encryptedPassword = passwordEncoder.encode(dto.getPassword());
        User user = User.builder()
                .email(dto.getEmail())
                .password(encryptedPassword)
                .nickname(dto.getNickname())
                .active(true)
                .role(dto.getRole() != null ? dto.getRole() : Role.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    public void updateNickname(String email, String newNickname) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setNickname(newNickname);
        userRepository.save(user);  // 변경 감지로도 가능하나 명시적으로 저장
    }

    public void deactivate(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setActive(false);
        userRepository.save(user);
    }

}
