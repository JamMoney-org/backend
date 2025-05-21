package com.example.jammoney.exception;

import lombok.Getter;

public class StockLogicException extends RuntimeException {
  @Getter
  private ErrorCode errorCode;

  public StockLogicException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
