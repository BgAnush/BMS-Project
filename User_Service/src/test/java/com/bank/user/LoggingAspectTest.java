package com.bank.user;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bank.user.aop.LoggingAspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingAspectTest {

    private LoggingAspect loggingAspect;

    private ProceedingJoinPoint pjp;
    private Signature signature;

    @BeforeEach
    void setUp() {

        loggingAspect = new LoggingAspect();

        pjp = mock(ProceedingJoinPoint.class);
        signature = mock(Signature.class);

        when(signature.getName())
                .thenReturn("testMethod");

        when(pjp.getSignature())
                .thenReturn(signature);

        when(pjp.getArgs())
                .thenReturn(new Object[]{"arg1", 100});

        when(pjp.getTarget())
                .thenReturn(new Object() {
                    @Override
                    public String toString() {
                        return "TestTarget";
                    }
                });
    }

    // =========================================================
    // CONTROLLER TESTS
    // =========================================================

    @Test
    void testLogControllerMethodsSuccess() throws Throwable {

        when(pjp.proceed())
                .thenReturn("SUCCESS");

        Object result =
                loggingAspect.logControllerMethods(pjp);

        assertEquals("SUCCESS", result);

        verify(pjp, times(1)).proceed();
    }

    @Test
    void testLogControllerMethodsException() throws Throwable {

        when(pjp.proceed())
                .thenThrow(new RuntimeException("Controller Error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> loggingAspect.logControllerMethods(pjp)
        );

        assertEquals("Controller Error", ex.getMessage());

        verify(pjp, times(1)).proceed();
    }

    // =========================================================
    // SERVICE TESTS
    // =========================================================

    @Test
    void testLogServiceMethodsSuccess() throws Throwable {

        when(pjp.proceed())
                .thenReturn("SERVICE_OK");

        Object result =
                loggingAspect.logServiceMethods(pjp);

        assertEquals("SERVICE_OK", result);

        verify(pjp, times(1)).proceed();
    }

    @Test
    void testLogServiceMethodsException() throws Throwable {

        when(pjp.proceed())
                .thenThrow(new RuntimeException("Service Error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> loggingAspect.logServiceMethods(pjp)
        );

        assertEquals("Service Error", ex.getMessage());

        verify(pjp, times(1)).proceed();
    }

    // =========================================================
    // REPOSITORY TESTS
    // =========================================================

    @Test
    void testLogRepositoryMethodsSuccess() throws Throwable {

        when(pjp.proceed())
                .thenReturn("REPO_OK");

        Object result =
                loggingAspect.logRepositoryMethods(pjp);

        assertEquals("REPO_OK", result);

        verify(pjp, times(1)).proceed();
    }

    @Test
    void testLogRepositoryMethodsException() throws Throwable {

        when(pjp.proceed())
                .thenThrow(new RuntimeException("Repository Error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> loggingAspect.logRepositoryMethods(pjp)
        );

        assertEquals("Repository Error", ex.getMessage());

        verify(pjp, times(1)).proceed();
    }

    // =========================================================
    // CLIENT TESTS
    // =========================================================

    @Test
    void testLogClientMethodsSuccess() throws Throwable {

        when(pjp.proceed())
                .thenReturn("CLIENT_OK");

        Object result =
                loggingAspect.logClientMethods(pjp);

        assertEquals("CLIENT_OK", result);

        verify(pjp, times(1)).proceed();
    }

    @Test
    void testLogClientMethodsException() throws Throwable {

        when(pjp.proceed())
                .thenThrow(new RuntimeException("Client Error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> loggingAspect.logClientMethods(pjp)
        );

        assertEquals("Client Error", ex.getMessage());

        verify(pjp, times(1)).proceed();
    }

    // =========================================================
    // UTIL TESTS
    // =========================================================

    @Test
    void testLogUtilMethodsSuccess() throws Throwable {

        when(pjp.proceed())
                .thenReturn("UTIL_OK");

        Object result =
                loggingAspect.logUtilMethods(pjp);

        assertEquals("UTIL_OK", result);

        verify(pjp, times(1)).proceed();
    }

    @Test
    void testLogUtilMethodsException() throws Throwable {

        when(pjp.proceed())
                .thenThrow(new RuntimeException("Util Error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> loggingAspect.logUtilMethods(pjp)
        );

        assertEquals("Util Error", ex.getMessage());

        verify(pjp, times(1)).proceed();
    }
}