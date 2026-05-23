package com.bank.creditcard;

import com.bank.card.exception.GlobalExceptionHandler;
import com.bank.card.exception.ApiError;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Internal Error");
        ResponseEntity<ApiError> response = handler.handleGeneric(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Error", response.getBody().getMessage());
    }
}