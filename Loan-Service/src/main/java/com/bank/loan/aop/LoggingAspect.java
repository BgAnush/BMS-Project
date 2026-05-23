package com.bank.loan.aop;

import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // =========================================================
    // CONTROLLER METHODS
    // =========================================================

    @Around("execution(* com.bank.loan.controller.*.*(..))")
    public Object logControllerMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        Object[] args = pjp.getArgs();

        log.info(
                "CONTROLLER START :: {}.{} :: args={}",
                className,
                methodName,
                Arrays.toString(args)
        );

        long start = System.currentTimeMillis();

        try {

            Object result = pjp.proceed();

            long end = System.currentTimeMillis();

            log.info(
                    "CONTROLLER SUCCESS :: {}.{} :: result={} :: time={} ms",
                    className,
                    methodName,
                    result,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "CONTROLLER ERROR :: {}.{} :: error={} :: time={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }

    // =========================================================
    // SERVICE METHODS
    // =========================================================

    @Around("execution(* com.bank.loan.service.*.*(..))")
    public Object logServiceMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        Object[] args = pjp.getArgs();

        log.info(
                "SERVICE START :: {}.{} :: args={}",
                className,
                methodName,
                Arrays.toString(args)
        );

        long start = System.currentTimeMillis();

        try {

            Object result = pjp.proceed();

            long end = System.currentTimeMillis();

            log.info(
                    "SERVICE SUCCESS :: {}.{} :: result={} :: time={} ms",
                    className,
                    methodName,
                    result,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "SERVICE ERROR :: {}.{} :: error={} :: time={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }

    // =========================================================
    // REPOSITORY METHODS
    // =========================================================

    @Around("execution(* com.bank.loan.repository.*.*(..))")
    public Object logRepositoryMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        Object[] args = pjp.getArgs();

        log.debug(
                "REPOSITORY START :: {}.{} :: args={}",
                className,
                methodName,
                Arrays.toString(args)
        );

        long start = System.currentTimeMillis();

        try {

            Object result = pjp.proceed();

            long end = System.currentTimeMillis();

            log.debug(
                    "REPOSITORY SUCCESS :: {}.{} :: time={} ms",
                    className,
                    methodName,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "REPOSITORY ERROR :: {}.{} :: error={} :: time={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }
}