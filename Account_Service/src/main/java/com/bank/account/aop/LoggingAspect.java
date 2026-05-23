package com.bank.account.aop;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // ================= POINTCUTS =================

    @Pointcut("execution(* com.bank.account.controller.*.*(..))")
    public void controller() {}

    @Pointcut("execution(* com.bank.account.service.*.*(..))")
    public void service() {}

    // ================= CONTROLLER LOG =================

    @Before("controller()")
    public void logController(JoinPoint jp) {

        log.info(
                "CONTROLLER : {} | args={}",
                jp.getSignature().toShortString(),
                Arrays.toString(jp.getArgs())
        );
    }

    // ================= SERVICE START =================

    @Before("service()")
    public void logServiceStart(JoinPoint jp) {

        log.info(
                "SERVICE START : {} | args={}",
                jp.getSignature().toShortString(),
                Arrays.toString(jp.getArgs())
        );
    }

    // ================= SERVICE SUCCESS =================

    @AfterReturning(pointcut = "service()", returning = "result")
    public void logServiceSuccess(JoinPoint jp, Object result) {

        log.info(
                "SERVICE SUCCESS : {} | result={}",
                jp.getSignature().toShortString(),
                result
        );
    }

    // ================= SERVICE ERROR =================

    @AfterThrowing(pointcut = "service()", throwing = "ex")
    public void logServiceError(JoinPoint jp, Throwable ex) {

        log.error(
                "SERVICE ERROR : {} | error={}",
                jp.getSignature().toShortString(),
                ex.getMessage()
        );
    }

    // ================= EXECUTION TIME =================

    @Around("service()")
    public Object measureTime(ProceedingJoinPoint pjp) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = pjp.proceed();

        long time = System.currentTimeMillis() - start;

        log.info(
                "{} executed in {} ms",
                pjp.getSignature().toShortString(),
                time
        );

        return result;
    }
}