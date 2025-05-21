package com.example.jammoney.exception;

public class CashNotFoundException extends RuntimeException {
  public CashNotFoundException() {
    super(ErrorCode.CASH_NOT_FOUND.getMessage());
  }

  public ErrorCode getErrorCode() {
    return ErrorCode.CASH_NOT_FOUND;
  }
}
