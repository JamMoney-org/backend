package com.example.jammoney.exception;

public class NicknameAlreadyExistsException extends CustomException{
    public NicknameAlreadyExistsException() {
        super(ErrorCode.NICKNAME_ALREADY_EXISTS);
    }
}
