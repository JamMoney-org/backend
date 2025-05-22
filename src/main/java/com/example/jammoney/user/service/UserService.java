package com.example.jammoney.user.service;

import com.example.jammoney.exception.*;
import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.cash.entity.Cash;
import com.example.jammoney.stockApp.stock.entity.UserPortfolio;
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
            throw new EmailAlreadyExistsException();
        }
        if(userRepository.existsByNickname(dto.getNickname())) {
            throw new NicknameAlreadyExistsException();
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        String encryptedPassword = passwordEncoder.encode(dto.getPassword());

        // 유저 생성
        User user = User.builder()
                .email(dto.getEmail())
                .password(encryptedPassword)
                .nickname(dto.getNickname())
                .isActive(true)
                .role(dto.getRole() != null ? dto.getRole() : Role.ROLE_USER)
                .build();

        // Pet 생성 (초기값 지정)
        Pet pet = Pet.builder()
                .user(user)
                .name("나의 친구")   // 초기 이름 지정
                .level(1)
                .exp(0)
                .mood("Happy")
                .build();
        user.setPet(pet);

        UserPortfolio userPortfolio = UserPortfolio.builder()
                .user(user)
                .cash(0)
                .stockAsset(0)
                .totalAsset(0)
                .profitAmount(0)
                .profitRate(0.0)
                .build();
        user.setUserPortfolio(userPortfolio);

        Cash cash = new Cash();
        cash.setUser(user);
        cash.setMoney(0); // 초기 자산 설정
        user.setCash(cash);     // 연관관계 설정

        // 저장 (Cascade에 의해 pet, cash도 같이 저장됨)
        userRepository.save(user);
    }

    public void updateNickname(String email, String newNickname) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setNickname(newNickname);
        userRepository.save(user);  // 변경 감지로도 가능하나 명시적으로 저장
    }

    public void deactivate(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.setActive(false);
        userRepository.save(user);
    }

}
