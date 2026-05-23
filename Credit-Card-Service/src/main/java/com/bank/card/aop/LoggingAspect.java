package com.bank.card.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // ================= CONTROLLER =================

    @Pointcut("execution(* com.bank.card.controller.*.*(..))")
    public void controllerLayer() {}

    // ================= SERVICE =================

    @Pointcut("execution(* com.bank.card.service.*.*(..))")
    public void serviceLayer() {}

    // ================= CONTROLLER LOG =================

    @Before("controllerLayer()")
    public void logController(JoinPoint jp) {
        log.info("📩 CONTROLLER CALL -> {}", jp.getSignature().toShortString());
    }

    // ================= SERVICE START =================

    @Before("serviceLayer()")
    public void logServiceStart(JoinPoint jp) {
        log.info("➡ SERVICE START -> {} | args={}",
                jp.getSignature().toShortString(),
                jp.getArgs());
    }

    // ================= SERVICE SUCCESS =================

    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logServiceSuccess(JoinPoint jp, Object result) {
        log.info("✔ SERVICE SUCCESS -> {} | result={}",
                jp.getSignature().toShortString(),
                result);
    }

    // ================= SERVICE ERROR =================

    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logServiceError(JoinPoint jp, Throwable ex) {
        log.error("❌ SERVICE ERROR -> {} | error={}",
                jp.getSignature().toShortString(),
                ex.getMessage());
    }

    // ================= EXECUTION TIME =================

    @Around("serviceLayer()")
    public Object executionTime(ProceedingJoinPoint pjp) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = pjp.proceed();

        long time = System.currentTimeMillis() - start;

        log.info("⏱ {} executed in {} ms",
                pjp.getSignature().getName(),
                time);

        return result;
    }
}