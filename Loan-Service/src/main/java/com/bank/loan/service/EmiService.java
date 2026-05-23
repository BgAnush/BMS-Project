package com.bank.loan.service;

import com.bank.loan.client.AccountClient;
import com.bank.loan.dto.EmiResponseDTO;
import com.bank.loan.entity.EMI;
import com.bank.loan.entity.Loan;
import com.bank.loan.exception.BadRequestException;
import com.bank.loan.exception.ResourceNotFoundException;
import com.bank.loan.repository.EMIRepository;
import com.bank.loan.repository.LoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmiService {

    private final EMIRepository emiRepo;
    private final LoanRepository loanRepo;
    private final AccountClient accountClient;

    // ================= EMI SCHEDULE =================

    public List<EmiResponseDTO> getEmiSchedule(Long userId, String loanNumber) {

        log.info("Fetching EMI schedule | userId={} loanNumber={}", userId, loanNumber);

        Loan loan = validateLoanOwnership(userId, loanNumber);

        List<EmiResponseDTO> list = emiRepo.findByLoanIdOrderByEmiNumberAsc(loan.getLoanId())
                .stream()
                .map(this::mapToDTO)
                .toList();

        log.info("EMI schedule fetched | userId={} loanId={} count={}",
                userId, loan.getLoanId(), list.size());

        return list;
    }

    // ================= PAY EMI =================

    public String payEmi(Long userId, String loanNumber, Integer emiNumber) {

        log.info("EMI payment request | userId={} loanNumber={} emiNumber={}",
                userId, loanNumber, emiNumber);

        Loan loan = validateLoanOwnership(userId, loanNumber);

        EMI emi = emiRepo.findByLoanIdAndEmiNumber(loan.getLoanId(), emiNumber)
                .orElseThrow(() -> {
                    log.error("EMI not found | loanId={} emiNumber={}",
                            loan.getLoanId(), emiNumber);
                    return new ResourceNotFoundException("EMI not found");
                });

        validateEmiPayment(loan, emi);

        log.info("Debiting account for EMI | accountNumber={} amount={}",
                loan.getAccountNumber(), emi.getAmount());

        accountClient.debit(loan.getAccountNumber(), emi.getAmount());

        emi.setStatus("PAID");
        emi.setPaidDate(LocalDate.now());
        emiRepo.save(emi);

        log.info("EMI paid successfully | loanId={} emiNumber={} amount={}",
                loan.getLoanId(), emiNumber, emi.getAmount());

        return String.format("EMI %d paid successfully", emiNumber);
    }

    // ================= CURRENT MONTH EMI =================

    public List<EmiResponseDTO> getCurrentMonthEmi(Long userId) {

        log.info("Fetching next payable EMI | userId={}", userId);

        List<Loan> loans = loanRepo.findByUserId(userId);

        if (loans.isEmpty()) {
            log.warn("No loans found for user | userId={}", userId);
            return List.of();
        }

        List<Long> loanIds = loans.stream()
                .map(Loan::getLoanId)
                .toList();

        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);

        List<EmiResponseDTO> list = emiRepo.findByLoanIdIn(loanIds)
                .stream()

                // ✅ Only NOT PAID EMIs
                .filter(e -> !"PAID".equals(e.getStatus()))

                // ✅ Only current OR next month EMI
                .filter(e -> {
                    boolean isCurrentMonth =
                            e.getDueDate().getMonth() == today.getMonth() &&
                            e.getDueDate().getYear() == today.getYear();

                    boolean isNextMonth =
                            e.getDueDate().getMonth() == nextMonth.getMonth() &&
                            e.getDueDate().getYear() == nextMonth.getYear();

                    return isCurrentMonth || isNextMonth;
                })
                .sorted((e1, e2) -> Integer.compare(e1.getEmiNumber(), e2.getEmiNumber()))

                .limit(1)

                .map(this::mapToDTO)
                .toList();

        log.info("Next payable EMI fetched | userId={} count={}", userId, list.size());

        return list;
    }

    // ================= ADMIN =================

    public List<EMI> getAllEmis() {

        log.info("Fetching all EMI records");

        List<EMI> list = emiRepo.findAll();

        log.info("Total EMI records count={}", list.size());

        return list;
    }

    // ================= INTERNAL LOGIC =================

    private Loan validateLoanOwnership(Long userId, String loanNumber) {

        log.debug("Validating loan ownership | userId={} loanNumber={}", userId, loanNumber);

        Loan loan = loanRepo.findByLoanNumber(loanNumber)
                .orElseThrow(() -> {
                    log.error("Loan not found | loanNumber={}", loanNumber);
                    return new ResourceNotFoundException("Loan not found");
                });

        if (!loan.getUserId().equals(userId)) {
            log.error("Unauthorized loan access | userId={} loanNumber={}", userId, loanNumber);
            throw new BadRequestException("Unauthorized access");
        }

        if (!"ACTIVE".equals(loan.getStatus())) {
            log.warn("Inactive loan access | loanNumber={}", loanNumber);
            throw new BadRequestException("Loan not active");
        }

        return loan;
    }

    private void validateEmiPayment(Loan loan, EMI emi) {

        LocalDate today = LocalDate.now();
        LocalDate nextMonth = today.plusMonths(1);

        log.debug("Validating EMI payment | loanId={} emiNumber={}",
                loan.getLoanId(), emi.getEmiNumber());

        if ("PAID".equals(emi.getStatus())) {
            log.warn("EMI already paid | loanId={} emiNumber={}",
                    loan.getLoanId(), emi.getEmiNumber());
            throw new BadRequestException("EMI already paid");
        }

        boolean isCurrentMonth =
                emi.getDueDate().getMonth() == today.getMonth() &&
                emi.getDueDate().getYear() == today.getYear();

        boolean isNextMonth =
                emi.getDueDate().getMonth() == nextMonth.getMonth() &&
                emi.getDueDate().getYear() == nextMonth.getYear();

        if (!(isCurrentMonth || isNextMonth)) {
            log.warn("Invalid EMI payment attempt | loanId={} emiNumber={} dueDate={}",
                    loan.getLoanId(), emi.getEmiNumber(), emi.getDueDate());
            throw new BadRequestException("You can only pay current or next month's EMI");
        }

        if (emi.getDueDate().isBefore(today)) {
            log.error("Overdue EMI | loanId={} emiNumber={}",
                    loan.getLoanId(), emi.getEmiNumber());
            throw new BadRequestException("EMI overdue. Contact bank");
        }

        boolean previousPending = emiRepo
                .findByLoanIdAndEmiNumberLessThan(loan.getLoanId(), emi.getEmiNumber())
                .stream()
                .anyMatch(e -> !"PAID".equals(e.getStatus()));

        if (previousPending) {
            log.warn("Previous EMI pending | loanId={} emiNumber={}",
                    loan.getLoanId(), emi.getEmiNumber());
            throw new BadRequestException("Clear previous EMI first");
        }
    }

    private EmiResponseDTO mapToDTO(EMI emi) {
        return new EmiResponseDTO(
                emi.getEmiNumber(),
                emi.getDueDate(),
                emi.getAmount(),
                emi.getStatus()
        );
    }

    public void generateEmiSchedule(Loan loan) {

        log.info("Generating EMI schedule | loanId={} tenure={} emiAmount={}",
                loan.getLoanId(), loan.getTenureMonths(), loan.getEmiAmount());

        if (loan.getEmiAmount() == null) {
            log.error("EMI amount not calculated | loanId={}", loan.getLoanId());
            throw new BadRequestException("EMI amount not calculated");
        }

        for (int i = 1; i <= loan.getTenureMonths(); i++) {

            EMI emi = EMI.builder()
                    .loanId(loan.getLoanId())
                    .emiNumber(i)
                    .amount(loan.getEmiAmount())
                    .status("PENDING")
                    .dueDate(LocalDate.now().plusMonths(i))
                    .build();

            emiRepo.save(emi);

            log.debug("EMI created | loanId={} emiNumber={} dueDate={}",
                    loan.getLoanId(), i, emi.getDueDate());
        }

        log.info("EMI schedule generation completed | loanId={}", loan.getLoanId());
    }
}