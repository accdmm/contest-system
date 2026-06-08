package com.contest.common.result;

import java.io.Serializable;

/**
 * 统一响应体 — 前后端数据交换的标准格式
 *
 * 所有 Controller 接口统一返回 Result&lt;T&gt;，确保前端可以按统一结构解析响应：
 * <pre>
 * {
 *   "code": 200,       // 状态码
 *   "message": "成功",  // 提示信息
 *   "data": {...}      // 实际数据
 * }
 * </pre>
 *
 * 状态码设计说明：
 * - 2xx — 成功（如 200 成功）
 * - 3xx — 业务相关异常（如 301 账号已存在、451 重复报名）
 * - 4xx — 客户端错误（如 400 参数错误、401 未登录、403 无权访问）
 * - 5xx — 服务器错误（如 500 服务器内部异常）
 *
 * 可用性说明：code 用于前端判断业务状态（非 HTTP 状态码），message 可直接展示给用户，
 * data 为泛型数据体。前端通过响应拦截器统一处理：code=200 时取 data，其他 code 时提示 message。
 *
 * @param <T> 响应数据类型
 */
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private boolean success;
    private long timestamp;

    private Result() {}

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = this.code == 200;
        this.timestamp = System.currentTimeMillis();
    }

    private Result(ResultCodeEnum resultCodeEnum, T data) {
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
        this.data = data;
        this.success = this.code == 200;
        this.timestamp = System.currentTimeMillis();
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
    public boolean isSuccess() { return success; }
    public long getTimestamp() { return timestamp; }
}
