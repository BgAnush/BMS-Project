package com.bank.loan;

import com.bank.loan.client.AccountClient;
import com.bank.loan.dto.EmiResponseDTO;
import com.bank.loan.entity.EMI;
import com.bank.loan.entity.Loan;
import com.bank.loan.exception.BadRequestException;
import com.bank.loan.exception.ResourceNotFoundException;
import com.bank.loan.repository.EMIRepository;
import com.bank.loan.repository.LoanRepository;
import com.bank.loan.service.EmiService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmiServiceTest {

    @Mock
    private EMIRepository emiRepo;

    @Mock
    private LoanRepository loanRepo;

    @Mock
    private AccountClient accountClient;

    @InjectMocks
    private EmiService service;

    private Loan testLoan;
    private final Long userId = 10L;
    private final String loanNumber = "LN-100";

    @BeforeEach
    void setup() {
        testLoan = Loan.builder()
                .loanId(1L)
                .userId(userId)
                .loanNumber(loanNumber)
                .accountNumber("ACC-TEST-99")
                .status("ACTIVE")
                .emiAmount(2500.0)
                .tenureMonths(3)
                .build();
    }

    // ================= GET EMI SCHEDULE TESTS =================

    @Test
    @DisplayName("Should return EMI list when loan exists and user is authorized")
    void getEmiSchedule_Success() {
        EMI emi = EMI.builder()
                .loanId(1L)
                .emiNumber(1)
                .amount(2500.0)
                .build();

        when(loanRepo.findByLoanNumber(loanNumber)).thenReturn(Optional.of(testLoan));
        when(emiRepo.findByLoanIdOrderByEmiNumberAsc(1L)).thenReturn(List.of(emi));

        List<EmiResponseDTO> result = service.getEmiSchedule(userId, loanNumber);

        assertEquals(1, result.size());
        assertEquals(2500.0, result.get(0).getAmount());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when loan number is invalid")
    void getEmiSchedule_NotFound() {
        when(loanRepo.findByLoanNumber("INVALID")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getEmiSchedule(userId, "INVALID"));
    }

    @Test
    @DisplayName("Should throw BadRequestException when user does not own the loan")
    void getEmiSchedule_Unauthorized() {
        testLoan.setUserId(999L);

        when(loanRepo.findByLoanNumber(loanNumber)).thenReturn(Optional.of(testLoan));

        assertThrows(BadRequestException.class,
                () -> service.getEmiSchedule(userId, loanNumber));
    }

    // ================= PAY EMI TESTS =================

    @Test
    @DisplayName("Should successfully pay EMI when conditions are met")
    void payEmi_Success() {
        EMI emi = EMI.builder()
                .loanId(1L)
                .emiNumber(1)
                .status("PENDING")
                .amount(2500.0)
                .dueDate(LocalDate.now())
                .build();

        when(loanRepo.findByLoanNumber(loanNumber)).thenReturn(Optional.of(testLoan));
        when(emiRepo.findByLoanIdAndEmiNumber(1L, 1)).thenReturn(Optional.of(emi));
        when(emiRepo.findByLoanIdAndEmiNumberLessThan(anyLong(), anyInt()))
                .thenReturn(List.of());

        String response = service.payEmi(userId, loanNumber, 1);

        assertTrue(response.contains("paid successfully"));
        assertEquals("PAID", emi.getStatus());

        // ✅ FIXED: removed eq()
        verify(accountClient).debit("ACC-TEST-99", 2500.0);
        verify(emiRepo).save(emi);
    }

    @Test
    @DisplayName("Should fail if EMI is already paid")
    void payEmi_AlreadyPaid() {
        EMI emi = EMI.builder().status("PAID").build();

        when(loanRepo.findByLoanNumber(loanNumber)).thenReturn(Optional.of(testLoan));
        when(emiRepo.findByLoanIdAndEmiNumber(1L, 1)).thenReturn(Optional.of(emi));

        assertThrows(BadRequestException.class,
                () -> service.payEmi(userId, loanNumber, 1));
    }

    @Test
    @DisplayName("Should fail if trying to pay a future EMI out of sequence")
    void payEmi_FutureEmiSequenceCheck() {

        // ✅ Loan must be ACTIVE
        testLoan.setStatus("ACTIVE");

        EMI emi = EMI.builder()
                .loanId(1L)
                .status("PENDING")
                .emiNumber(2)
                .amount(2500.0)
                .dueDate(LocalDate.now()) // ✅ IMPORTANT
                .build();

        EMI prevEmi = EMI.builder()
                .loanId(1L)
                .status("PENDING") // ❗ Not PAID → triggers sequence failure
                .emiNumber(1)
                .amount(2500.0)
                .dueDate(LocalDate.now().minusMonths(1)) // ✅ IMPORTANT
                .build();

        when(loanRepo.findByLoanNumber(loanNumber))
                .thenReturn(Optional.of(testLoan));

        when(emiRepo.findByLoanIdAndEmiNumber(1L, 2))
                .thenReturn(Optional.of(emi));

        when(emiRepo.findByLoanIdAndEmiNumberLessThan(1L, 2))
                .thenReturn(List.of(prevEmi));

        assertThrows(BadRequestException.class,
                () -> service.payEmi(userId, loanNumber, 2));

        // ✅ VERY IMPORTANT (no money should be deducted)
        verify(accountClient, never()).debit(anyString(), anyDouble());
    }

    // ================= CURRENT MONTH DASHBOARD TESTS =================

    @Test
    @DisplayName("Should return only PENDING EMIs for the current month")
    void getCurrentMonthEmi_FilterLogic() {
        EMI currentEmi = EMI.builder()
                .loanId(1L)
                .status("PENDING")
                .dueDate(LocalDate.now())
                .build();

        EMI pastEmi = EMI.builder()
                .loanId(1L)
                .status("PAID")
                .dueDate(LocalDate.now().minusMonths(2))
                .build();

        when(loanRepo.findByUserId(userId)).thenReturn(List.of(testLoan));
        when(emiRepo.findByLoanIdIn(anyList()))
                .thenReturn(Arrays.asList(currentEmi, pastEmi));

        List<EmiResponseDTO> result = service.getCurrentMonthEmi(userId);

        assertEquals(1, result.size());
    }

    // ================= EMI GENERATION TESTS =================

    @Test
    @DisplayName("Should generate correct number of EMIs based on tenure")
    void generateEmiSchedule_LogicCheck() {
        service.generateEmiSchedule(testLoan);

        verify(emiRepo, times(3)).save(any(EMI.class));
    }

    @Test
    @DisplayName("Should throw error if loan data is incomplete during generation")
    void generateEmiSchedule_IncompleteData() {
        testLoan.setEmiAmount(null);

        assertThrows(BadRequestException.class,
                () -> service.generateEmiSchedule(testLoan));
    }

    @Test
    @DisplayName("Should return all EMIs in the system (Admin functionality)")
    void getAllEmis_Success() {
        when(emiRepo.findAll()).thenReturn(List.of(new EMI(), new EMI()));

        List<EMI> result = service.getAllEmis();

        assertEquals(2, result.size());
        verify(emiRepo).findAll();
    }
}