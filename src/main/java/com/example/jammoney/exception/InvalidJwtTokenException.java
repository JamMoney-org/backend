package com.example.jammoney.exception;

public class InvalidJwtTokenException extends CustomException {
    public InvalidJwtTokenException() {
        super(ErrorCode.INVALID_TOKEN);
    }
}
