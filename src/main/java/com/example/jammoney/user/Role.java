package com.example.jammoney.user;

public enum Role {
    ROLE_ADMIN,
    ROLE_USER;
    public String getRole() {
        return name();
    }
}
