package com.canteen.common.result;

import lombok.Data;

/**
 * 统一返回结果
 * @param <T> 数据类型
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    // 成功状态码
    public static final Integer SUCCESS_CODE = 200;
    // 失败状态码
    public static final Integer ERROR_CODE = 500;
    // 未授权状态码
    public static final Integer UNAUTHORIZED_CODE = 401;

    public Result() {}

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "操作成功", null);
    }

    /**
     * 成功返回结果
     * @param data 返回数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }

    /**
     * 成功返回结果
     * @param message 返回消息
     * @param data 返回数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    /**
     * 失败返回结果
     * @param message 错误消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null);
    }

    /**
     * 失败返回结果
     * @param code 错误码
     * @param message 错误消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 未授权返回结果
     */
    public static <T> Result<T> unauthorized() {
        return new Result<>(UNAUTHORIZED_CODE, "未授权访问", null);
    }

    /**
     * 未授权返回结果
     * @param message 错误消息
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(UNAUTHORIZED_CODE, message, null);
    }
}