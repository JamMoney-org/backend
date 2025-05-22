package com.example.jammoney.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 공통 에러 응답 생성 메서드
    private ResponseEntity<ErrorResponseDto> buildErrorResponse(ErrorCode code, HttpServletRequest request, String overrideMessage) {
        return ResponseEntity.status(code.getStatus()).body(
                new ErrorResponseDto(
                        code.getStatus(),
                        code.name(),
                        overrideMessage != null ? overrideMessage : code.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    private ResponseEntity<ErrorResponseDto> buildErrorResponse(ErrorCode code, HttpServletRequest request) {
        return buildErrorResponse(code, request, null);
    }

    // 2. ErrorCode 기반의 커스텀 예외 처리 (모든 XxxException)
    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            NicknameAlreadyExistsException.class,
            PasswordMismatchException.class,
            UserNotFoundException.class,
            InvalidRefreshTokenException.class,
            InvalidJwtTokenException.class,
            CashNotFoundException.class,
            InsufficientBalanceException.class,
            StockLogicException.class
    })
    public ResponseEntity<ErrorResponseDto> handleCustomException(RuntimeException e, HttpServletRequest request) {
        if (e instanceof CustomException custom) {
            return buildErrorResponse(custom.getErrorCode(), request);
        }
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, request);
    }

    // 3. 로그인 실패 (비밀번호 틀림 등)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentials(BadCredentialsException e, HttpServletRequest request) {
        return buildErrorResponse(ErrorCode.INVALID_LOGIN, request, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }

    // 4. @Valid 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationError(MethodArgumentNotValidException e, HttpServletRequest request) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : ErrorCode.VALIDATION_ERROR.getMessage();
        return buildErrorResponse(ErrorCode.VALIDATION_ERROR, request, message);
    }

    // 5. DB 제약조건 위반
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleDataIntegrityViolation(DataIntegrityViolationException e, HttpServletRequest request) {
        return buildErrorResponse(ErrorCode.EMAIL_ALREADY_EXISTS, request); // 상황 따라 다른 ErrorCode 매핑 가능
    }

    // 6. 처리되지 않은 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnhandledException(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception caught: ", e);
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, request);
    }
}

