package com.contest.common.result;

/**
 * 统一响应体
 */
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private Result() {}

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    private Result(ResultCodeEnum resultCodeEnum, T data) {
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
        this.data = data;
    }

    /**
     * 返回成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCodeEnum.SUCCESS, data);
    }

    /**
     * 返回成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCodeEnum.SUCCESS, null);
    }

    /**
     * 返回错误响应（根据枚举）
     *
     * @param resultCodeEnum 错误状态码枚举
     * @param <T>            数据类型
     * @return 错误响应
     */
    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        return new Result<>(resultCodeEnum, null);
    }

    /**
     * 返回错误响应（自定义状态码和消息）
     *
     * @param code    错误状态码
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 错误响应
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 返回错误响应（默认 400 状态码）
     *
     * @param message 错误信息
     * @param <T>     数据类型
     * @return 错误响应
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCodeEnum.FAIL.getCode(), message, null);
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
