package com.example.jammoney.exception;

public class LevelNotMatchedException extends CustomException {
  public LevelNotMatchedException() {
    super(ErrorCode.LEVEL_NOT_MATCHED);
  }
}