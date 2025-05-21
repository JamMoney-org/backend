package com.example.jammoney.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
    }

    public ErrorCode getErrorCode() {
        return ErrorCode.INSUFFICIENT_BALANCE;
    }
}
