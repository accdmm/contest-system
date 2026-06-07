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
 * 操作日志切面，自动记录带有 @OperationLog 注解的方法调用
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
     * 环绕通知，在方法执行后记录操作日志
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
