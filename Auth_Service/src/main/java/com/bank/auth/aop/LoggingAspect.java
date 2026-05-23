package com.bank.auth.aop;

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

    // =========================================================
    // POINTCUTS
    // =========================================================

    @Pointcut("execution(* com.bank.auth.controller.*.*(..))")
    public void controllerLayer() {}

    @Pointcut("execution(* com.bank.auth.service.*.*(..))")
    public void serviceLayer() {}

    // =========================================================
    // CONTROLLER LOGS
    // =========================================================

    @Before("controllerLayer()")
    public void logControllerRequest(JoinPoint jp) {

        log.info(
                "CONTROLLER START :: {} :: args={}",
                jp.getSignature().toShortString(),
                Arrays.toString(jp.getArgs())
        );
    }

    @AfterReturning(
            pointcut = "controllerLayer()",
            returning = "result"
    )
    public void logControllerResponse(
            JoinPoint jp,
            Object result
    ) {

        log.info(
                "CONTROLLER END :: {} :: response={}",
                jp.getSignature().toShortString(),
                result
        );
    }

    // =========================================================
    // SERVICE LOGS
    // =========================================================

    @Before("serviceLayer()")
    public void logServiceStart(JoinPoint jp) {

        log.info(
                "SERVICE START :: {} :: args={}",
                jp.getSignature().toShortString(),
                Arrays.toString(jp.getArgs())
        );
    }

    @AfterReturning(
            pointcut = "serviceLayer()",
            returning = "result"
    )
    public void logServiceSuccess(
            JoinPoint jp,
            Object result
    ) {

        log.info(
                "SERVICE SUCCESS :: {} :: result={}",
                jp.getSignature().toShortString(),
                result
        );
    }

    @AfterThrowing(
            pointcut = "serviceLayer()",
            throwing = "ex"
    )
    public void logServiceError(
            JoinPoint jp,
            Throwable ex
    ) {

        log.error(
                "SERVICE ERROR :: {} :: error={}",
                jp.getSignature().toShortString(),
                ex.getMessage()
        );
    }

    // =========================================================
    // EXECUTION TIME
    // =========================================================

    @Around("serviceLayer()")
    public Object measureExecutionTime(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        long start = System.currentTimeMillis();

        Object result = pjp.proceed();

        long end = System.currentTimeMillis();

        log.info(
                "EXECUTION TIME :: {} :: {} ms",
                pjp.getSignature().toShortString(),
                (end - start)
        );

        return result;
    }
}