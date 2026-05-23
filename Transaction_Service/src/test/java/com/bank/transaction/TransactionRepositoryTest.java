package com.bank.transaction;

import com.bank.transaction.model.Transaction;
import com.bank.transaction.repository.TransactionRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository repo;

    // =========================================================
    // SAVE + FIND SUCCESS
    // =========================================================
    @Test
    void save_and_findByAccountNumberOrTargetAccountNumber() {

        Transaction transaction = new Transaction();

        transaction.setAccountNumber("123456");
        transaction.setTargetAccountNumber("654321");
        transaction.setAmount(1000.0);
        transaction.setType("TRANSFER");
        transaction.setTransactionDate(LocalDateTime.now());

        repo.save(transaction);

        List<Transaction> result =
                repo.findByAccountNumberOrTargetAccountNumber(
                        "123456",
                        "654321"
                );

        assertNotNull(result);
        assertFalse(result.isEmpty());

        Transaction savedTransaction = result.get(0);

        assertEquals("123456", savedTransaction.getAccountNumber());
        assertEquals("654321", savedTransaction.getTargetAccountNumber());
        assertEquals("TRANSFER", savedTransaction.getType());
        assertEquals(1000.0, savedTransaction.getAmount());
    }

    // =========================================================
    // EMPTY RESULT TEST
    // =========================================================
    @Test
    void findByAccountNumberOrTargetAccountNumber_shouldReturnEmptyList() {

        List<Transaction> result =
                repo.findByAccountNumberOrTargetAccountNumber(
                        "NO_ACC",
                        "NO_TARGET"
                );

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}