package com.example.jammoney.exception;

import lombok.Getter;

@Getter
public class PasswordMismatchException extends CustomException {
    public PasswordMismatchException() {
        super(ErrorCode.PASSWORD_MISMATCH);
    }
}
