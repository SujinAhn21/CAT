// src/main/java/com/example/coin/common/ErrorCode.java

package com.example.coin.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 공통 에러 코드 정의.
 * 실제로 많이 쓸 것들만 최소한으로 정의해둔다.
 */
@Getter
public enum ErrorCode
{
    INVALID_REQUEST("COMMON_INVALID_REQUEST", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED("COMMON_UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN("COMMON_FORBIDDEN", HttpStatus.FORBIDDEN, "접근이 허용되지 않았습니다."),
    NOT_FOUND("COMMON_NOT_FOUND", HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_ERROR("COMMON_INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage)
    {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
