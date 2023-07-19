package com.zj.exception;

import com.zj.common.ErrorCode;

/**
 * 自定义全局业务异常类
 *
 * @author 1720400789
 */
public class BussinessException extends RuntimeException{

    private final int code;

    private final String description;

    public BussinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BussinessException(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BussinessException(ErrorCode errorCode, String description){
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
