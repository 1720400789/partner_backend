package com.zj.common;

/**
 * 返回工具类
 *
 * @author 1720400789
 */
public class ResultUtils {

    /**
     * 成功响应
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败响应
     * @param errorCode
     * @param <T>
     * @return
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message, String description){
        return new BaseResponse(errorCode.getCode(), null, message, description);
    }

    public static <T> BaseResponse<T> error(ErrorCode errorCode, String description){
        return new BaseResponse(errorCode.getCode(), null, errorCode.getMessage(), description);
    }

    /**
     * 失败
     * @param code
     * @param message
     * @param description
     * @return
     */
    public static BaseResponse error(int code, String message, String description){
        return new BaseResponse(code, null, message, description);
    }
}