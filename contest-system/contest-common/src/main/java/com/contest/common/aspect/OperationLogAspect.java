package com.contest.common.aspect;

import com.contest.common.annotation.OperationLog;
import com.contest.common.event.OperationLogEvent;
import com.contest.common.security.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 操作日志切面 — 通过 AOP 自动记录管理员操作
 *
 * <p>使用 @Around("@annotation(operationLog)") 切入点表达式，拦截所有
 * 标注 @OperationLog 注解的 Controller 方法。在方法执行成功后，
 * 通过 Spring 事件机制异步写入操作日志表。
 *
 * <p>事件驱动设计说明：Aspect 发布 OperationLogEvent 事件，
 * OperationLogEventListener 监听事件并写入数据库。避免了 Aspect 直接
 * 注入 Service 可能导致的循环依赖（common → message → common）。
 *
 * <p>安全性说明：仅记录登录用户的操作，未登录时不记录。
 * 日志内容包含操作类型、参数关键信息和请求 IP，便于审计追踪。
 */
@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest request;

    public OperationLogAspect(ApplicationEventPublisher eventPublisher, HttpServletRequest request) {
        this.eventPublisher = eventPublisher;
        this.request = request;
    }

    /**
     * 环绕通知：方法执行成功后记录操作日志
     *
     * <p>先执行目标方法（joinPoint.proceed()），执行成功后再收集参数并发布事件。
     * 这样设计确保：即使日志记录失败也不影响业务操作。
     */
    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        Object result = joinPoint.proceed();

        try {
            Long userId = SecurityUtil.getCurrentUserId();
            if (userId == null) return result;

            StringBuilder detail = new StringBuilder(operationLog.action());
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof Long || arg instanceof String) {
                    detail.append(" [").append(arg).append("]");
                }
            }

            eventPublisher.publishEvent(new OperationLogEvent(userId, operationLog.action(), detail.toString(), request.getRemoteAddr()));
        } catch (Exception e) {
            log.warn("记录操作日志失败", e);
        }

        return result;
    }
}
