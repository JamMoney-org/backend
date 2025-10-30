package com.example.jammoney.exception;

public class BlacklistedTokenException extends CustomException {
    public BlacklistedTokenException() {
        super(ErrorCode.BLACKLISTED_TOKEN);
    }
}
