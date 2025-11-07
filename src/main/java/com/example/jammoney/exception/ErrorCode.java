package com.example.jammoney.exception;
import lombok.Getter;

@Getter
public enum ErrorCode {
    PASSWORD_MISMATCH("비밀번호와 비밀번호 재입력이 일치하지 않습니다.",409),
    EMAIL_ALREADY_EXISTS("이미 사용 중인 이메일입니다.", 409),
    NICKNAME_ALREADY_EXISTS("이미 사용 중인 닉네임입니다.", 409),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", 410),
    VALIDATION_ERROR("요청 값이 유효하지 않습니다.", 400),
    LEVEL_NOT_MATCHED("레벨 7 미달성 시 현금을 생성할 수 없습니다.",411),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", 500),
    INVALID_LOGIN("존재하지 않는 이메일이거나, 올바르지 않은 비밀번호입니다.",410),
    INVALID_TOKEN("유효하지 않은 JWT 토큰입니다.",401),
    EXPIRED_TOKEN("만료된 토큰입니다.",401),
    INVALID_REFRESH_TOKEN("유요하지 않은 refresh_token 입니다.", 401),
    BLACKLISTED_TOKEN("이미 로그아웃된 토큰입니다.",401),
    CASH_NOT_FOUND("해당 사용자의 Cash 정보가 없습니다.", 411),
    INSUFFICIENT_BALANCE("잔액이 부족합니다.", 411),
    INSUFFICIENT_STOCK("보유 주식이 부족합니다.",413),
    ORDER_NOT_FOUND("존재하지 않는 거래 내역입니다.",414),
    ORDER_PERMISSION_DENIED("잘못된 삭제 요청입니다.",415),
    ORDER_ALREADY_FINISH("이미 완료된 거래입니다.",416),
    HOLDING_STOCK_NOT_FOUND("보유하고 있지 않은 주식입니다.",417 ),
    ASKING_PRICE_NOT_FOUND("호가 정보를 찾을 수 없습니다.",418);
    private final String message;
    private final int status;

    ErrorCode(String message, int status) {
        this.message = message;
        this.status = status;
    }
}
