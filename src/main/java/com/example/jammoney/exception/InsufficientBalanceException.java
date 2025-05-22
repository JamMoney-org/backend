package com.example.jammoney.exception;

public class InsufficientBalanceException extends CustomException {
    public InsufficientBalanceException() {
        super(ErrorCode.INSUFFICIENT_BALANCE);
    }
}
