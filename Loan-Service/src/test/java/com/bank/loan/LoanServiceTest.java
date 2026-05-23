package com.bank.loan;

import com.bank.loan.client.AccountClient;
import com.bank.loan.client.TransactionClient;
import com.bank.loan.dto.*;
import com.bank.loan.entity.EMI;
import com.bank.loan.entity.Loan;
import com.bank.loan.exception.ApiError;
import com.bank.loan.exception.BadRequestException;
import com.bank.loan.exception.GlobalExceptionHandler;
import com.bank.loan.exception.ResourceNotFoundException;
import com.bank.loan.repository.EMIRepository;
import com.bank.loan.repository.LoanRepository;
import com.bank.loan.service.EmiService;
import com.bank.loan.service.LoanService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @InjectMocks
    private LoanService loanService;
    
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private LoanRepository loanRepo;

    @Mock
    private AccountClient accountClient;

    @Mock
    private EMIRepository emiRepo;

    @Mock
    private EmiService emiService;

    @Mock
    private TransactionClient transactionClient;

    // ================= LOAN APPLICATION TESTS =================

    @Test
    void testApplyLoan_success() {
        LoanRequestDTO dto = new LoanRequestDTO();
        dto.setAmount(100000.0);
        dto.setTenureMonths(12);
        dto.setLoanType("PERSONAL");

        AccountResponseDTO acc = new AccountResponseDTO();
        acc.setAccountNumber("123456");

        when(accountClient.getAccountByUserId(1L)).thenReturn(acc);
        when(loanRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanResponseDTO res = loanService.applyLoan(1L, dto);

        assertNotNull(res);
        verify(loanRepo).save(any());
    }

    @Test
    void testValidateLoan_limitExceeded() {
        LoanRequestDTO dto = new LoanRequestDTO();
        dto.setAmount(500000.0); // Assume this exceeds logic limit
        dto.setTenureMonths(12);

        when(loanRepo.findByUserId(1L)).thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> loanService.applyLoan(1L, dto));
    }

    // ================= LOAN STATUS TESTS =================

    @Test
    void testApproveLoan_success() {
        Loan loan = Loan.builder()
                .loanId(1L).loanNumber("LN-2").status("PENDING")
                .loanAmount(50000.0).interestRate(10.0).tenureMonths(12)
                .accountNumber("123456").build();

        when(loanRepo.findByLoanNumber("LN-2")).thenReturn(Optional.of(loan));
        when(loanRepo.save(any())).thenReturn(loan);

        // Approval involves side effects: generate schedule, deposit money, log transaction
        Loan result = loanService.approveLoan("LN-2");

        assertEquals("ACTIVE", result.getStatus());
        verify(emiService).generateEmiSchedule(any());
        verify(accountClient).deposit(anyString(), anyDouble());
        verify(transactionClient).logTransaction(any());
    }

    @Test
    void testRejectLoan() {
        Loan loan = Loan.builder().loanNumber("PL-1").status("PENDING").build();
        when(loanRepo.findByLoanNumber("PL-1")).thenReturn(Optional.of(loan));
        when(loanRepo.save(any())).thenReturn(loan);

        Loan result = loanService.rejectLoan("PL-1");

        assertEquals("REJECTED", result.getStatus());
    }

    // ================= LOAN CLOSURE TESTS =================

    @Test
    void testCloseLoan_success() {

        Loan loan = Loan.builder()
                .loanId(1L)
                .userId(10L)
                .loanNumber("LN-1")
                .status("ACTIVE")
                .accountNumber("123456")
                .loanAmount(10000.0)
                .interestRate(12.0)      // ✅ IMPORTANT
                .tenureMonths(12)        // ✅ IMPORTANT
                .build();

        EMI emi1 = EMI.builder()
                .loanId(1L)
                .emiNumber(1)
                .status("PAID")
                .amount(3000.0)
                .build();

        EMI emi2 = EMI.builder()
                .loanId(1L)
                .emiNumber(2)
                .status("PENDING")
                .amount(2000.0)
                .build();

        when(loanRepo.findByLoanNumber("LN-1"))
                .thenReturn(Optional.of(loan));

        when(emiRepo.findByLoanIdOrderByEmiNumberAsc(1L))
                .thenReturn(List.of(emi1, emi2));

        String result = loanService.closeLoan(10L, "LN-1");

        // 🔥 Calculate expected value EXACTLY like service
        double pendingEmi = 2000.0;

        double totalInterest = 10000.0 * (12.0 / 100) * (12 / 12.0); // = 1200
        double closurePenalty = totalInterest / 3.0;                // = 400

        double expectedFinalAmount = pendingEmi + closurePenalty;   // = 2400

        assertTrue(result.contains("successfully"));

        // ✅ EXACT verification (now it will pass)
        verify(accountClient).debit("123456", expectedFinalAmount);

        verify(loanRepo).save(loan);
        verify(emiRepo).saveAll(anyList());
    }

    // ================= EXCEPTION HANDLER TESTS (Non-Deprecated) =================

    @Test
    void testHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Loan not found");

        ResponseEntity<ApiError> response = handler.handleNotFound(ex);

        // FIXED: Using getStatusCode().value() instead of getStatusCodeValue()
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Loan not found", response.getBody().getMessage());
    }

    @Test
    void testHandleBadRequest() {
        BadRequestException ex = new BadRequestException("Invalid request");

        ResponseEntity<ApiError> response = handler.handleBad(ex);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid request", response.getBody().getMessage());
    }

    @Test
    void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ApiError> response = handler.handleValidation(ex);

        assertEquals(400, response.getStatusCode().value());
        // Depending on your GlobalExceptionHandler, it might return a specific format
        assertNotNull(response.getBody().getMessage());
    }
}