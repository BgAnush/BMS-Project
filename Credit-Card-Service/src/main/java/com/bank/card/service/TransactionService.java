package com.bank.card.service;

import com.bank.card.client.AccountClient;
import com.bank.card.client.TransactionClient;
import com.bank.card.dto.CardBillResponseDTO;
import com.bank.card.dto.TransactionDTO;
import com.bank.card.dto.TransactionRequestDTO;
import com.bank.card.exception.*;
import com.bank.card.model.CardTransaction;
import com.bank.card.model.CreditCard;
import com.bank.card.enums.*;
import com.bank.card.repository.CreditCardRepository;
import com.bank.card.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final CreditCardRepository cardRepo;
    private final TransactionRepository txRepo;
    private final AccountClient accountClient;
    private final TransactionClient transactionClient;

    private static final double MIN_DUE_PERCENT = 0.05;
    private static final double MIN_DUE_FLAT = 500.0;
    private static final double LATE_PENALTY = 500.0;
    private static final double INTEREST_RATE = 0.03;
    private static final int BILLING_DAY = 28;

    // ================= CARD SWIPE =================
    @Transactional
    public String process(Long userId, TransactionDTO dto) {

        log.info("Card transaction initiated | userId={} card={}", userId, dto.getCardNumber());

        CreditCard card = cardRepo.findByCardNumber(dto.getCardNumber())
                .orElseThrow(() -> {
                    log.error("Card not found | card={}", dto.getCardNumber());
                    return new ResourceNotFoundException("Card not found");
                });

        validateCardStatus(card, userId, dto);

        updateCardLimits(card, dto.getAmount());

        recordTransaction(card.getId(), dto.getAmount(),
                TransactionType.valueOf(dto.getType()), dto.getMerchant());

        log.info("Transaction successful | cardId={} amount={}", card.getId(), dto.getAmount());

        return "TRANSACTION SUCCESSFUL";
    }

    // ================= GET BILL DETAILS =================
    public CardBillResponseDTO getBillDetails(Long userId) {

        log.info("Fetching bill details | userId={}", userId);

        List<CreditCard> cards = getCardsOrThrow(userId);
        double totalDue = calculateTotalUsed(cards);

        log.info("Bill generated | userId={} totalDue={}", userId, totalDue);

        return createBillDTO(totalDue, 0.0, 0.0, 0.0, "Bill Generated");
    }

    // ================= PAY BILL =================
    @Transactional
    public CardBillResponseDTO payBill(Long userId, Double amount) {

        log.info("Bill payment initiated | userId={} amount={}", userId, amount);

        validateAmount(amount);
        List<CreditCard> cards = getCardsOrThrow(userId);
        double totalPrincipal = calculateTotalUsed(cards);

        if (totalPrincipal <= 0) {
            log.warn("No outstanding bill | userId={}", userId);
            throw new BadRequestException("No outstanding bill");
        }

        LocalDate dueDate = getDueDate();
        boolean isLate = LocalDate.now().isAfter(dueDate);
        double penalty = isLate ? LATE_PENALTY : 0;
        double interest = isLate ? (totalPrincipal * INTEREST_RATE) : 0;
        double totalPayable = totalPrincipal + penalty + interest;

        log.info("Bill calculation | userId={} principal={} penalty={} interest={} totalPayable={}",
                userId, totalPrincipal, penalty, interest, totalPayable);

        processAccountWithdrawal(userId, amount, totalPayable);

        double remainingForPrincipal = deductFees(amount, interest, penalty);

        if (remainingForPrincipal > 0) {
            applyPaymentToPrincipal(cards, remainingForPrincipal);
        }

        recordTransaction(null, amount, TransactionType.CARD_BILL_PAYMENT, "CARD_BILL_PAYMENT");

        syncExternalLog(userId, amount);

        log.info("Bill payment successful | userId={} paidAmount={}", userId, amount);

        return createBillDTO(totalPrincipal, amount, penalty, interest, "Payment successful");
    }

    // ================= HELPER METHODS =================

    private void validateCardStatus(CreditCard card, Long userId, TransactionDTO dto) {

        log.debug("Validating card | cardId={} userId={}", card.getId(), userId);

        if (!card.getUserId().equals(userId)) {
            log.error("Unauthorized access | userId={} cardId={}", userId, card.getId());
            throw new UnauthorizedException("Unauthorized");
        }

        if (!card.getStatus().equals(CardStatus.ACTIVE)) {
            log.warn("Inactive card used | cardId={}", card.getId());
            throw new BadRequestException("Inactive");
        }

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            log.warn("Expired card used | cardId={}", card.getId());
            throw new BadRequestException("Expired");
        }

        if (!card.getPin().equals(dto.getPin())) {
            log.error("Invalid PIN attempt | cardId={}", card.getId());
            throw new UnauthorizedException("Invalid PIN");
        }

        if (card.getAvailableLimit() < dto.getAmount()) {
            log.warn("Limit exceeded | cardId={} available={} requested={}",
                    card.getId(), card.getAvailableLimit(), dto.getAmount());
            throw new BadRequestException("Limit exceeded");
        }
    }

    private void processAccountWithdrawal(Long userId, Double amount, Double totalPayable) {

        log.info("Processing account withdrawal | userId={} amount={} totalPayable={}",
                userId, amount, totalPayable);

        if (amount > totalPayable) {
            log.warn("Payment exceeds payable | userId={}", userId);
            throw new BadRequestException("Exceeds payable: " + totalPayable);
        }

        var account = accountClient.getAccountByUserId(userId);

        if (account == null || account.getBalance() < amount) {
            log.error("Insufficient account balance | userId={}", userId);
            throw new BadRequestException("Insufficient balance in source account");
        }

        accountClient.withdraw(account.getAccountNumber(), amount);

        log.info("Withdrawal successful | account={}", account.getAccountNumber());
    }

    private double deductFees(double payment, double interest, double penalty) {

        log.debug("Deducting fees | payment={} interest={} penalty={}", payment, interest, penalty);

        double afterInterest = Math.max(0, payment - interest);
        return Math.max(0, afterInterest - penalty);
    }

    private void applyPaymentToPrincipal(List<CreditCard> cards, double amount) {

        log.info("Applying payment to principal | amount={}", amount);

        double remaining = amount;

        for (CreditCard card : cards) {

            double used = card.getUsedLimit();
            double paymentForCard = Math.min(remaining, Math.max(0, used));

            card.setUsedLimit(used - paymentForCard);
            card.setAvailableLimit(card.getCreditLimit() - card.getUsedLimit());

            remaining -= paymentForCard;

            if (remaining <= 0) break;
        }

        cardRepo.saveAll(cards);

        log.info("Principal payment distributed successfully");
    }

    private void recordTransaction(Long cardId, Double amount, TransactionType type, String merchant) {

        log.debug("Recording transaction | cardId={} amount={} type={}", cardId, amount, type);

        CardTransaction tx = new CardTransaction();
        tx.setCardId(cardId);
        tx.setAmount(amount);
        tx.setMerchant(merchant);
        tx.setType(type);
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setTransactionTime(LocalDateTime.now());

        txRepo.save(tx);

        log.debug("Transaction saved successfully");
    }

    private CardBillResponseDTO createBillDTO(double total, double paid, double penalty, double interest, String msg) {

        double minDue = Math.max(total * MIN_DUE_PERCENT, MIN_DUE_FLAT);
        double remainingDue = (total + penalty + interest) - paid;

        log.debug("Bill DTO created | total={} paid={} remaining={}", total, paid, remainingDue);

        return new CardBillResponseDTO(total, minDue, paid, remainingDue, penalty, interest, msg, getDueDate());
    }

    private void syncExternalLog(Long userId, Double amount) {

        try {
            var account = accountClient.getAccountByUserId(userId);

            transactionClient.logTransaction(
                    new TransactionRequestDTO(account.getAccountNumber(), null, amount, "CARD_PAYMENT")
            );

            log.info("External transaction log synced | userId={}", userId);

        } catch (Exception e) {
            log.error("External log failed | userId={} error={}", userId, e.getMessage());
        }
    }

    private List<CreditCard> getCardsOrThrow(Long userId) {

        List<CreditCard> cards = cardRepo.findByUserId(userId);

        if (cards.isEmpty()) {
            log.warn("No cards found | userId={}", userId);
            throw new BadRequestException("No cards found");
        }

        return cards;
    }

    private double calculateTotalUsed(List<CreditCard> cards) {

        double total = cards.stream().mapToDouble(CreditCard::getUsedLimit).sum();

        log.debug("Total used calculated = {}", total);

        return total;
    }

    private void updateCardLimits(CreditCard card, Double amount) {

        log.debug("Updating card limits | cardId={} amount={}", card.getId(), amount);

        card.setUsedLimit(card.getUsedLimit() + amount);
        card.setAvailableLimit(card.getCreditLimit() - card.getUsedLimit());

        cardRepo.save(card);
    }

    private LocalDate getDueDate() {
        return LocalDate.now().withDayOfMonth(BILLING_DAY);
    }

    private void validateAmount(Double amount) {

        if (amount == null || amount <= 0) {
            log.error("Invalid amount: {}", amount);
            throw new BadRequestException("Invalid amount");
        }
    }

    // ================= HISTORY =================
    public List<CardTransaction> getUserTransactions(Long userId) {

        log.info("Fetching user transactions | userId={}", userId);

        return txRepo.findAll().stream()
                .filter(tx -> isTransactionOwnedByUser(tx, userId))
                .toList();
    }

    private boolean isTransactionOwnedByUser(CardTransaction tx, Long userId) {

        if (tx.getCardId() == null) return true;

        return cardRepo.findById(tx.getCardId())
                .map(card -> card.getUserId().equals(userId))
                .orElse(false);
    }

    public List<CardTransaction> getAllTransactions() {

        log.info("Fetching ALL transactions");

        return txRepo.findAll();
    }
}