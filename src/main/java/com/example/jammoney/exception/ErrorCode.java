package com.example.jammoney.exception;
import lombok.Getter;
@Getter
public enum ErrorCode {
    EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다.", 409),
    VALIDATION_ERROR("요청 값이 유효하지 않습니다.", 400),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", 500),
    REFRESH_TOKEN_NOT_FOUND("리프레시 토큰이 존재하지 않습니다.", 401),
    REFRESH_TOKEN_EXPIRED("리프레시 토큰이 만료되었습니다.", 401);
    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
