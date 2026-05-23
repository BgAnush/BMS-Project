package com.bank.creditcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bank.card.dto.*;
import com.bank.card.enums.CardStatus;
import com.bank.card.enums.TransactionType;
import com.bank.card.exception.BadRequestException;
import com.bank.card.exception.ResourceNotFoundException;
import com.bank.card.exception.UnauthorizedException;
import com.bank.card.model.CreditCard;
import com.bank.card.model.CardTransaction;
import com.bank.card.repository.CreditCardRepository;
import com.bank.card.repository.TransactionRepository;
import com.bank.card.service.TransactionService;
import com.bank.card.client.AccountClient;
import com.bank.card.client.TransactionClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private CreditCardRepository cardRepo;
    @Mock private TransactionRepository txRepo;
    @Mock private AccountClient accountClient;
    @Mock private TransactionClient transactionClient;

    @InjectMocks
    private TransactionService service;

    private CreditCard card;

    @BeforeEach
    void setup() {
        card = new CreditCard();
        card.setId(1L);
        card.setUserId(1L);
        card.setPin("1234");
        card.setStatus(CardStatus.ACTIVE);
        card.setCreditLimit(1000.0);
        card.setAvailableLimit(1000.0);
        card.setUsedLimit(2000.0);
        card.setExpiryDate(LocalDate.now().plusDays(10));
    }

    // ================= PROCESS SUCCESS =================
    @Test
    void process_successTransaction() {

        TransactionDTO dto = new TransactionDTO();
        dto.setCardNumber("1111");
        dto.setAmount(100.0);
        dto.setPin("1234");
        dto.setMerchant("Amazon");
        dto.setType(TransactionType.PURCHASE.name());

        when(cardRepo.findByCardNumber("1111")).thenReturn(Optional.of(card));
        when(cardRepo.save(any())).thenReturn(card);
        when(txRepo.save(any(CardTransaction.class))).thenAnswer(i -> i.getArgument(0));

        String result = service.process(1L, dto);

        assertEquals("TRANSACTION SUCCESSFUL", result);
    }

    // ================= BILL DETAILS =================
    @Test
    void getBillDetails_success() {

        when(cardRepo.findByUserId(1L)).thenReturn(List.of(card));

        CardBillResponseDTO res = service.getBillDetails(1L);

        assertEquals(2000.0, res.getTotalDue());
        assertEquals("Bill Generated", res.getMessage());
    }

    @Test
    void getBillDetails_empty() {

        when(cardRepo.findByUserId(1L)).thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> service.getBillDetails(1L));
    }


    @Test
    void payBill_invalidAmount() {
        assertThrows(BadRequestException.class,
                () -> service.payBill(1L, -10.0));
    }

    @Test
    void payBill_noCards() {

        when(cardRepo.findByUserId(1L)).thenReturn(List.of());

        assertThrows(BadRequestException.class,
                () -> service.payBill(1L, 100.0));
    }

    @Test
    void payBill_accountNull() {

        when(cardRepo.findByUserId(1L)).thenReturn(List.of(card));
        when(accountClient.getAccountByUserId(1L)).thenReturn(null);

        assertThrows(BadRequestException.class,
                () -> service.payBill(1L, 100.0));
    }

    // ================= USER TRANSACTIONS =================
    @Test
    void getUserTransactions_success() {

        CardTransaction tx = new CardTransaction();
        tx.setCardId(1L);

        when(txRepo.findAll()).thenReturn(List.of(tx));
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        List<CardTransaction> res = service.getUserTransactions(1L);

        assertEquals(1, res.size());
    }

    // ================= ALL TRANSACTIONS =================
    @Test
    void getAllTransactions_success() {

        when(txRepo.findAll()).thenReturn(List.of(new CardTransaction()));

        List<CardTransaction> res = service.getAllTransactions();

        assertEquals(1, res.size());
    }
 // ================= PROCESS FAILURES =================

    @Test
    void process_cardNotFound_throwsException() {
        TransactionDTO dto = new TransactionDTO();
        dto.setCardNumber("9999");
        when(cardRepo.findByCardNumber("9999")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.process(1L, dto));
    }

    @Test
    void process_unauthorizedUser_throwsException() {
        TransactionDTO dto = new TransactionDTO();
        dto.setCardNumber("1111");
        // Card belongs to User 1, but User 2 is trying to use it
        when(cardRepo.findByCardNumber("1111")).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedException.class, () -> service.process(2L, dto));
    }

    @Test
    void process_invalidPin_throwsException() {
        TransactionDTO dto = new TransactionDTO();
        dto.setCardNumber("1111");
        dto.setPin("0000"); // Wrong PIN
        when(cardRepo.findByCardNumber("1111")).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedException.class, () -> service.process(1L, dto));
    }

    @Test
    void process_insufficientLimit_throwsException() {
        TransactionDTO dto = new TransactionDTO();
        dto.setCardNumber("1111");
        dto.setPin("1234");
        dto.setAmount(5000.0); // Limit is 1000
        when(cardRepo.findByCardNumber("1111")).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class, () -> service.process(1L, dto));
    }
 // ================= PAY BILL SUCCESS =================

    @Test
    void payBill_fullPayment_success() {
        // Setup: Card has 2000 used limit
        when(cardRepo.findByUserId(1L)).thenReturn(List.of(card));
        
        // Mock Account Response
        AccountResponseDTO account = new AccountResponseDTO();
        account.setAccountNumber("ACC123");
        account.setBalance(5000.0);
        when(accountClient.getAccountByUserId(1L)).thenReturn(account);

        // Act: Pay 1000 (Partial payment of the 2000 due)
        CardBillResponseDTO res = service.payBill(1L, 1000.0);

        // Assert
        assertNotNull(res);
        assertEquals(1000.0, res.getPaidAmount());
        assertEquals("Payment successful", res.getMessage());
        
        // Verify money was withdrawn from bank account
        verify(accountClient, times(1)).withdraw("ACC123", 1000.0);
        
        // Verify card limits were updated (Used limit should drop from 2000 to 1000)
        verify(cardRepo, times(1)).saveAll(any());
        assertEquals(1000.0, card.getUsedLimit());
    }

    @Test
    void payBill_exceedsTotalPayable_throwsException() {
        when(cardRepo.findByUserId(1L)).thenReturn(List.of(card));
        
        // Total due is 2000, trying to pay 3000
        assertThrows(BadRequestException.class, () -> service.payBill(1L, 3000.0));
    }
   
}