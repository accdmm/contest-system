package com.contest.common.exception;

import com.contest.common.result.Result;
import com.contest.common.result.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器 — 统一拦截各层异常并转换为 Result 响应
 *
 * <p>通过 @RestControllerAdvice 注解拦截 Controller 层抛出的各类异常，确保前端
 * 始终收到结构统一的错误响应，避免 500 白页或堆栈泄露。
 *
 * <p>可用性说明：用户看到的是中文错误提示（如"报名已截止"），而非技术堆栈。
 * 安全性说明：未捕获的异常仅记录日志，不向客户端暴露堆栈信息。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常（BusinessException）
     *
     * <p>业务异常由 Service 层在验证不通过时主动抛出，如"竞赛不存在""报名已截止"等。
     * 异常中携带自定义 code 和 message，直接透传给前端展示。
     *
     * @param e BusinessException 实例
     * @param response HTTP 响应
     * @return 结构化的错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e, HttpServletResponse response) {
        int httpStatus = e.getCode() >= 100 && e.getCode() < 600 ? e.getCode() : HttpStatus.BAD_REQUEST.value();
        response.setStatus(httpStatus);
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid / @Validated 触发）
     *
     * <p>Controller 中 @RequestBody 或 @RequestParam 上的校验注解（如 @NotBlank、@Size）
     * 校验失败时抛出 MethodArgumentNotValidException。将所有字段错误信息拼接后返回。
     *
     * <p>安全性说明：只返回校验失败原因，不泄露数据结构。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理参数绑定异常（BindException）
     *
     * <p>与 MethodArgumentNotValidException 类似，在简单类型参数绑定失败时抛出。
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return Result.error(ResultCodeEnum.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 处理缺少请求头异常（如请求未携带 Authorization Token）
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public Result<Void> handleMissingHeader(MissingRequestHeaderException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return Result.error(ResultCodeEnum.UNAUTHORIZED);
    }

    /**
     * 处理权限不足异常（@PreAuthorize 校验不通过）
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDenied(AccessDeniedException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        return Result.error(ResultCodeEnum.FORBIDDEN);
    }

    /**
     * 处理数据完整性冲突异常
     *
     * <p>数据库唯一约束冲突等场景触发，如重复手机号绑定。
     * 记录 warn 日志后提示"操作冲突"，不暴露具体冲突字段。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.warn("Data integrity violation", e);
        return Result.error(ResultCodeEnum.CONFLICT);
    }

    /**
     * 处理未捕获的异常 — 兜底处理
     *
     * <p>所有未在上面列出的异常在此统一处理。记录 error 日志（含完整堆栈）便于排查，
     * 但只返回 500 通用错误信息给客户端，防止堆栈信息泄露。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e, HttpServletResponse response) {
        log.error("Unhandled exception", e);
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return Result.error(ResultCodeEnum.SERVER_ERROR);
    }
}
