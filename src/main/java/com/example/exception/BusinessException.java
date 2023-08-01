package com.example.exception;

import com.example.common.ErrorCode;

/**
 * @Author: chenyim
 * @CreateTime: 2023-06-21  09:48
 * @Description: 异常处理
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

