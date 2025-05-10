package com.example.jammoney.exception;

import lombok.Getter;

@Getter
public class PasswordMismatchException extends RuntimeException {
    final ErrorCode errorCode;
    public PasswordMismatchException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
