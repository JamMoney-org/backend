package com.example.jammoney.exception;

public class InvalidJwtTokenException extends RuntimeException {
    private ErrorCode errorCode;
    public InvalidJwtTokenException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
