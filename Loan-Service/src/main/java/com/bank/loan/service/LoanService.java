package com.bank.loan.service;

import com.bank.loan.client.AccountClient;
import com.bank.loan.client.TransactionClient;
import com.bank.loan.dto.*;
import com.bank.loan.entity.EMI;
import com.bank.loan.entity.Loan;
import com.bank.loan.enums.LoanType;
import com.bank.loan.exception.*;
import com.bank.loan.repository.EMIRepository;
import com.bank.loan.repository.LoanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final EmiService emiService;
    private final LoanRepository loanRepo;
    private final AccountClient accountClient;
    private final EMIRepository emiRepo;
    private final TransactionClient transactionClient;

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final Random RANDOM_GENERATOR = new Random();

    // ================= USER =================

    public LoanResponseDTO applyLoan(Long userId, LoanRequestDTO dto) {

        log.info("Loan apply request | userId={} amount={} tenure={} type={}",
                userId, dto.getAmount(), dto.getTenureMonths(), dto.getLoanType());

        validateLoan(dto);

        AccountResponseDTO account = accountClient.getAccountByUserId(userId);

        validateUserLoanLimit(userId, dto.getAmount());

        if (account == null) {
            log.error("Account not found | userId={}", userId);
            throw new ResourceNotFoundException("Account not found");
        }

        Loan loan = Loan.builder()
                .userId(userId)
                .accountNumber(account.getAccountNumber())
                .loanAmount(dto.getAmount())
                .tenureMonths(dto.getTenureMonths())
                .loanType(dto.getLoanType())
                .loanNumber(generateLoanNumber(dto.getLoanType()))
                .interestRate(getInterest(dto.getLoanType()))
                .status(STATUS_PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        loanRepo.save(loan);

        log.info("Loan created successfully | loanId={} loanNumber={} userId={}",
                loan.getLoanId(), loan.getLoanNumber(), userId);

        return new LoanResponseDTO(
                loan.getLoanId(),
                loan.getLoanAmount(),
                loan.getStatus()
        );
    }

    // ================= MANAGER =================

    public Loan approveLoan(String loanNumber) {

        log.info("Loan approval request | loanNumber={}", loanNumber);

        Loan loan = getLoan(loanNumber);

        if (!STATUS_PENDING.equals(loan.getStatus())) {
            log.warn("Loan already processed | loanNumber={} status={}",
                    loanNumber, loan.getStatus());
            throw new BadRequestException("Loan already processed");
        }

        double emiAmount = calculateEMI(
                loan.getLoanAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths()
        );

        log.info("EMI calculated | loanNumber={} emiAmount={}", loanNumber, emiAmount);

        loan.setEmiAmount(emiAmount);
        loan.setStatus(STATUS_ACTIVE);

        Loan savedLoan = loanRepo.save(loan);

        log.info("Loan approved | loanNumber={} loanId={}", loanNumber, savedLoan.getLoanId());

        emiService.generateEmiSchedule(savedLoan);
        log.info("EMI schedule generated | loanId={}", savedLoan.getLoanId());

        accountClient.deposit(
                savedLoan.getAccountNumber(),
                savedLoan.getLoanAmount()
        );

        log.info("Loan amount credited | account={} amount={}",
                savedLoan.getAccountNumber(), savedLoan.getLoanAmount());

        transactionClient.logTransaction(
                new TransactionRequestDTO(
                        savedLoan.getAccountNumber(),
                        null,
                        savedLoan.getLoanAmount(),
                        "LOAN_CREDIT"
                )
        );

        log.info("Transaction logged for loan credit | loanNumber={}", loanNumber);

        return savedLoan;
    }

    public Loan rejectLoan(String loanNumber) {

        log.warn("Loan rejection request | loanNumber={}", loanNumber);

        Loan loan = getLoan(loanNumber);

        loan.setStatus(STATUS_REJECTED);

        Loan saved = loanRepo.save(loan);

        log.warn("Loan rejected | loanNumber={}", loanNumber);

        return saved;
    }

    public List<Loan> getPendingLoans() {

        log.info("Fetching pending loans");

        List<Loan> list = loanRepo.findAll()
                .stream()
                .filter(l -> STATUS_PENDING.equals(l.getStatus()))
                .toList();

        log.info("Pending loans count={}", list.size());

        return list;
    }

    public String closeLoan(Long userId, String loanNumber) {

        log.info("Loan closure request | userId={} loanNumber={}", userId, loanNumber);

        Loan loan = getLoan(loanNumber);

        if (!loan.getUserId().equals(userId)) {
            log.error("Unauthorized loan closure | userId={} loanNumber={}", userId, loanNumber);
            throw new BadRequestException("Unauthorized loan access");
        }

        if (!STATUS_ACTIVE.equals(loan.getStatus())) {
            log.warn("Loan not active for closure | loanNumber={}", loanNumber);
            throw new BadRequestException("Loan is not active");
        }

        List<EMI> emis = emiRepo.findByLoanIdOrderByEmiNumberAsc(loan.getLoanId());

        double pendingEmiAmount = emis.stream()
                .filter(e -> !"PAID".equals(e.getStatus()))
                .mapToDouble(EMI::getAmount)
                .sum();

        double totalInterest = loan.getLoanAmount()
                * (loan.getInterestRate() / 100)
                * (loan.getTenureMonths() / 12.0);

        double closurePenalty = totalInterest / 3.0;
        double finalAmount = pendingEmiAmount + closurePenalty;

        log.info("Loan closure calculation | loanNumber={} pending={} penalty={} finalAmount={}",
                loanNumber, pendingEmiAmount, closurePenalty, finalAmount);

        accountClient.debit(loan.getAccountNumber(), finalAmount);

        log.info("Amount debited for loan closure | account={} amount={}",
                loan.getAccountNumber(), finalAmount);

        loan.setStatus(STATUS_CLOSED);
        loanRepo.save(loan);

        emis.forEach(e -> e.setStatus(STATUS_CLOSED));
        emiRepo.saveAll(emis);

        log.info("Loan closed successfully | loanNumber={}", loanNumber);

        return "Loan closed successfully. Amount deducted: " + finalAmount;
    }

    // ================= INTERNAL =================

    private String generateLoanNumber(String type) {

        String prefix;

        switch (LoanType.valueOf(type.toUpperCase())) {
            case GOLD -> prefix = "GL";
            case PERSONAL -> prefix = "PL";
            case HOME -> prefix = "HL";
            default -> prefix = "LN";
        }

        String loanNumber = prefix + "-" + (10000000 + RANDOM_GENERATOR.nextInt(90000000));

        log.debug("Generated loan number={} for type={}", loanNumber, type);

        return loanNumber;
    }

    private double calculateEMI(double principal, double rate, int months) {

        double monthlyRate = rate / (12 * 100);

        double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, months)) /
                (Math.pow(1 + monthlyRate, months) - 1);

        log.debug("EMI calculated | principal={} rate={} months={} emi={}",
                principal, rate, months, emi);

        return emi;
    }

    private void validateUserLoanLimit(Long userId, Double newLoanAmount) {

        log.debug("Validating loan limits | userId={} newAmount={}", userId, newLoanAmount);

        List<Loan> userLoans = loanRepo.findByUserId(userId);

        List<Loan> activeLoans = userLoans.stream()
                .filter(l -> !STATUS_REJECTED.equals(l.getStatus()) && !STATUS_CLOSED.equals(l.getStatus()))
                .toList();

        if (activeLoans.size() >= 3) {
            log.warn("Loan count exceeded | userId={}", userId);
            throw new BadRequestException("Maximum 3 loans allowed per user");
        }

        double totalExistingLoan = activeLoans.stream()
                .mapToDouble(Loan::getLoanAmount)
                .sum();

        if (totalExistingLoan + newLoanAmount > 400000) {
            log.warn("Total loan limit exceeded | userId={}", userId);
            throw new BadRequestException("Total loan limit exceeded (Max 4 Lakhs)");
        }

        boolean hasMaxLoan = activeLoans.stream()
                .anyMatch(l -> l.getLoanAmount() >= 400000);

        if (hasMaxLoan) {
            log.warn("User already has max loan | userId={}", userId);
            throw new BadRequestException("User already has a maximum loan of 4 Lakhs");
        }
    }

    public List<Loan> getUserLoans(Long userId) {
        log.info("Fetching user loans | userId={}", userId);
        return loanRepo.findByUserId(userId);
    }

    public List<Loan> getAllLoans() {
        log.info("Fetching all loans");
        return loanRepo.findAll();
    }

    public void deleteLoan(String loanNumber) {
        log.error("Deleting loan | loanNumber={}", loanNumber);
        loanRepo.delete(getLoan(loanNumber));
        log.error("Loan deleted | loanNumber={}", loanNumber);
    }

    private Loan getLoan(String loanNumber) {

        return loanRepo.findByLoanNumber(loanNumber)
                .orElseThrow(() -> {
                    log.error("Loan not found | loanNumber={}", loanNumber);
                    return new ResourceNotFoundException("Loan not found");
                });
    }

    private void validateLoan(LoanRequestDTO dto) {

        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            log.error("Invalid loan amount={}", dto.getAmount());
            throw new BadRequestException("Invalid amount");
        }

        if (dto.getAmount() > 1200000) {
            log.warn("Loan amount exceeds max limit={}", dto.getAmount());
            throw new BadRequestException("Max loan limit is 12L");
        }

        if (dto.getAmount() <= 50000 && dto.getTenureMonths() > 12) {
            log.warn("Invalid tenure for small loan | amount={} tenure={}",
                    dto.getAmount(), dto.getTenureMonths());
            throw new BadRequestException("Max 12 months for <= 50K");
        }

        if (dto.getTenureMonths() < 6 || dto.getTenureMonths() > 60) {
            log.warn("Invalid tenure | tenure={}", dto.getTenureMonths());
            throw new BadRequestException("Tenure must be 6–60 months");
        }
    }

    private double getInterest(String type) {
        return switch (type.toUpperCase()) {
            case "GOLD" -> 7.5;
            case "HOME" -> 8.5;
            case "PERSONAL" -> 12.0;
            default -> 10.0;
        };
    }
}