package com.example.jammoney.exception;

public class InvalidLoginException extends CustomException {
    public InvalidLoginException() {
        super(ErrorCode.INVALID_LOGIN);
    }
}
