// src/main/java/com/example/coin/common/ApiResponse.java

package com.example.coin.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * API 공통 응답 래퍼.
 * - success: 성공 여부
 * - data   : 실제 응답 데이터
 * - errorCode, message: 실패 시 에러 정보
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ApiResponse<T>
{
    private boolean success;
    private T data;
    private String errorCode;
    private String message;

    public static <T> ApiResponse<T> ok(T data)
    {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode code, String message)
    {
        String msg = (message != null && !message.isBlank())
                ? message
                : code.getDefaultMessage();
        return new ApiResponse<>(false, null, code.getCode(), msg);
    }
}
