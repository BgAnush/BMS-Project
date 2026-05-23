package com.bank.auth;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bank.auth.aop.LoggingAspect;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoggingAspectTest {

    private LoggingAspect loggingAspect;

    private JoinPoint joinPoint;
    private ProceedingJoinPoint proceedingJoinPoint;
    private Signature signature;

    @BeforeEach
    void setUp() {

        loggingAspect = new LoggingAspect();

        joinPoint = mock(JoinPoint.class);
        proceedingJoinPoint = mock(ProceedingJoinPoint.class);
        signature = mock(Signature.class);

        when(signature.toShortString())
                .thenReturn("TestMethod()");

        when(joinPoint.getSignature())
                .thenReturn(signature);

        when(proceedingJoinPoint.getSignature())
                .thenReturn(signature);
    }

    // =========================================================
    // CONTROLLER TESTS
    // =========================================================

    @Test
    void testLogControllerRequest() {

        Object[] args = {"test", 123};

        when(joinPoint.getArgs()).thenReturn(args);

        assertDoesNotThrow(() ->
                loggingAspect.logControllerRequest(joinPoint)
        );

        verify(joinPoint, times(1)).getArgs();
        verify(joinPoint, times(1)).getSignature();
    }

    @Test
    void testLogControllerResponse() {

        Object response = "SUCCESS";

        assertDoesNotThrow(() ->
                loggingAspect.logControllerResponse(
                        joinPoint,
                        response
                )
        );

        verify(joinPoint, times(1)).getSignature();
    }

    // =========================================================
    // SERVICE TESTS
    // =========================================================

    @Test
    void testLogServiceStart() {

        Object[] args = {"data"};

        when(joinPoint.getArgs()).thenReturn(args);

        assertDoesNotThrow(() ->
                loggingAspect.logServiceStart(joinPoint)
        );

        verify(joinPoint, times(1)).getArgs();
        verify(joinPoint, times(1)).getSignature();
    }

    @Test
    void testLogServiceSuccess() {

        Object result = "SERVICE_OK";

        assertDoesNotThrow(() ->
                loggingAspect.logServiceSuccess(
                        joinPoint,
                        result
                )
        );

        verify(joinPoint, times(1)).getSignature();
    }

    @Test
    void testLogServiceError() {

        Throwable throwable =
                new RuntimeException("Something went wrong");

        assertDoesNotThrow(() ->
                loggingAspect.logServiceError(
                        joinPoint,
                        throwable
                )
        );

        verify(joinPoint, times(1)).getSignature();
    }

    // =========================================================
    // EXECUTION TIME TEST
    // =========================================================

    @Test
    void testMeasureExecutionTime() throws Throwable {

        String expectedResult = "RESULT";

        when(proceedingJoinPoint.proceed())
                .thenReturn(expectedResult);

        Object actualResult =
                loggingAspect.measureExecutionTime(
                        proceedingJoinPoint
                );

        assertEquals(expectedResult, actualResult);

        verify(proceedingJoinPoint, times(1))
                .proceed();

        verify(proceedingJoinPoint, times(1))
                .getSignature();
    }

    @Test
    void testMeasureExecutionTimeThrowsException() throws Throwable {

        when(proceedingJoinPoint.proceed())
                .thenThrow(new RuntimeException("Execution failed"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> loggingAspect.measureExecutionTime(
                        proceedingJoinPoint
                )
        );

        assertEquals(
                "Execution failed",
                exception.getMessage()
        );

        verify(proceedingJoinPoint, times(1))
                .proceed();
    }
}