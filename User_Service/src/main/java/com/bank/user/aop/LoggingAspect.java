package com.bank.user.aop;

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
    // CONTROLLER LOGGING
    // =========================================================

    @Around("execution(* com.bank.user.controller.*.*(..))")
    public Object logControllerMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        Object[] args =
                pjp.getArgs();

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
                    "CONTROLLER SUCCESS :: {}.{} :: result={} :: executionTime={} ms",
                    className,
                    methodName,
                    result,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "CONTROLLER ERROR :: {}.{} :: error={} :: executionTime={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }

    // =========================================================
    // SERVICE LOGGING
    // =========================================================

    @Around("execution(* com.bank.user.service.*.*(..))")
    public Object logServiceMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        Object[] args =
                pjp.getArgs();

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
                    "SERVICE SUCCESS :: {}.{} :: result={} :: executionTime={} ms",
                    className,
                    methodName,
                    result,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "SERVICE ERROR :: {}.{} :: error={} :: executionTime={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }

    // =========================================================
    // REPOSITORY LOGGING
    // =========================================================

    @Around("execution(* com.bank.user.repository.*.*(..))")
    public Object logRepositoryMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        Object[] args =
                pjp.getArgs();

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
                    "REPOSITORY SUCCESS :: {}.{} :: executionTime={} ms",
                    className,
                    methodName,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "REPOSITORY ERROR :: {}.{} :: error={} :: executionTime={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }

    // =========================================================
    // CLIENT LOGGING
    // =========================================================

    @Around("execution(* com.bank.user.client.*.*(..))")
    public Object logClientMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        Object[] args =
                pjp.getArgs();

        log.info(
                "CLIENT START :: {}.{} :: args={}",
                className,
                methodName,
                Arrays.toString(args)
        );

        long start = System.currentTimeMillis();

        try {

            Object result = pjp.proceed();

            long end = System.currentTimeMillis();

            log.info(
                    "CLIENT SUCCESS :: {}.{} :: result={} :: executionTime={} ms",
                    className,
                    methodName,
                    result,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "CLIENT ERROR :: {}.{} :: error={} :: executionTime={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }

    // =========================================================
    // UTIL LOGGING
    // =========================================================

    @Around("execution(* com.bank.user.util.*.*(..))")
    public Object logUtilMethods(
            ProceedingJoinPoint pjp
    ) throws Throwable {

        String className =
                pjp.getTarget().getClass().getSimpleName();

        String methodName =
                pjp.getSignature().getName();

        long start = System.currentTimeMillis();

        try {

            Object result = pjp.proceed();

            long end = System.currentTimeMillis();

            log.debug(
                    "UTIL EXECUTED :: {}.{} :: executionTime={} ms",
                    className,
                    methodName,
                    (end - start)
            );

            return result;

        } catch (Exception ex) {

            long end = System.currentTimeMillis();

            log.error(
                    "UTIL ERROR :: {}.{} :: error={} :: executionTime={} ms",
                    className,
                    methodName,
                    ex.getMessage(),
                    (end - start)
            );

            throw ex;
        }
    }
}