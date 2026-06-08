package com.contest.common.exception;

import com.contest.common.result.ResultCodeEnum;

/**
 * 业务异常 — Service 层校验不通过时主动抛出
 *
 * 继承 RuntimeException，触发全局事务回滚。区别于技术异常（如 NullPointerException），
 * BusinessException 携带明确的业务状态码和中文提示，最终由 GlobalExceptionHandler 统一
 * 转换为 Result 响应返回前端。
 *
 * 使用场景：
 * - 参数校验失败（如"密码长度需为8-20位"）
 * - 业务规则不满足（如"报名已截止""竞赛不存在"）
 * - 权限验证不通过（如"仅队长可进行此操作"）
 *
 * 事务说明：抛出此异常时 Spring @Transactional 会检测到 RuntimeException 并回滚事务，
 * 确保数据一致性。
 */
public class BusinessException extends RuntimeException {

    private int code;

    /**
     * 构造业务异常（使用默认 400 状态码）
     *
     * @param message 错误提示，将直接展示给前端用户
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCodeEnum.FAIL.getCode();
    }

    /**
     * 构造业务异常（自定义状态码）
     *
     * @param code    业务状态码，与 ResultCodeEnum 保持一致
     * @param message 错误提示
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造业务异常（从枚举中获取状态码和消息）
     *
     * @param resultCodeEnum 状态码枚举
     */
    public BusinessException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }

    public int getCode() {
        return code;
    }

    /**
     * 将状态码反查为枚举
     *
     * @return 对应的状态码枚举，未找到时返回 FAIL
     */
    public ResultCodeEnum getCodeEnum() {
        for (ResultCodeEnum e : ResultCodeEnum.values()) {
            if (e.getCode() == code) return e;
        }
        return ResultCodeEnum.FAIL;
    }
}
