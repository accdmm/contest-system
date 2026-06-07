package com.contest.common.exception;

import com.contest.common.result.ResultCodeEnum;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {

    private int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCodeEnum.FAIL.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    public int getCode() {
        return code;
    }

    public ResultCodeEnum getCodeEnum() {
        for (ResultCodeEnum e : ResultCodeEnum.values()) {
            if (e.getCode() == code) return e;
        }
        return ResultCodeEnum.FAIL;
    }
}
