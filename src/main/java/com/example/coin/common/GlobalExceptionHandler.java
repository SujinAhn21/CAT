// src/main/java/com/example/coin/common/GlobalExceptionHandler.java

package com.example.coin.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리 핸들러.
 * 컨트롤러에서 던진 예외를 공통 포맷으로 변환한다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e)
    {
        log.warn("IllegalArgumentException", e);
        return build(ErrorCode.INVALID_REQUEST, e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e)
    {
        log.warn("IllegalStateException", e);
        return build(ErrorCode.INVALID_REQUEST, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception e)
    {
        log.warn("ValidationException", e);
        return build(ErrorCode.INVALID_REQUEST, "요청 파라미터가 올바르지 않습니다.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e)
    {
        log.error("Unhandled exception", e);
        return build(ErrorCode.INTERNAL_ERROR, null);
    }

    private ResponseEntity<ApiResponse<Void>> build(ErrorCode code, String message)
    {
        ApiResponse<Void> body = ApiResponse.error(code, message);
        return ResponseEntity.status(code.getHttpStatus()).body(body);
    }
}
