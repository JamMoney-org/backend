package com.example.jammoney.user.dto;

import com.example.jammoney.user.entity.User;
import lombok.Getter;

@Getter
public class UserProfileDto {
    private Long id;
    private String email;
    private String nickname;

    public UserProfileDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
    }
}