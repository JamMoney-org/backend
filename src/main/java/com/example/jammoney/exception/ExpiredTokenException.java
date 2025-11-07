package com.example.jammoney.exception;

public class ExpiredTokenException extends CustomException{

  public ExpiredTokenException() {
    super(ErrorCode.EXPIRED_TOKEN);
  }
}
