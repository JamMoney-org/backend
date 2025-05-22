package com.example.jammoney.exception;

public class CashNotFoundException extends CustomException {
  public CashNotFoundException() {
    super(ErrorCode.CASH_NOT_FOUND);
  }
}
