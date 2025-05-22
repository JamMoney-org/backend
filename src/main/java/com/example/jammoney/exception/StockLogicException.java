package com.example.jammoney.exception;

import lombok.Getter;

public class StockLogicException extends CustomException {
  public StockLogicException(ErrorCode errorCode) {
    super(errorCode);
  }
}
