package com.bank.card.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.bank.card.model.CreditCard;
import com.bank.card.repository.CreditCardRepository;
import com.bank.card.dto.CardRequestDTO;
import com.bank.card.dto.PinRequestDTO;
import com.bank.card.enums.CardStatus;
import com.bank.card.enums.CardType;
import com.bank.card.util.CardUtil;
import com.bank.card.exception.*;

@Slf4j
@Service
public class CreditCardService {

    private final CreditCardRepository repo;

    public CreditCardService(CreditCardRepository repo) {
        this.repo = repo;
    }

    // ========================= USER =========================

    public CreditCard requestCard(Long userId, CardRequestDTO dto) {

        log.info("Card request initiated | userId={} type={}", userId, dto.getType());

        if (dto.getType() == null) {
            log.error("Card type missing | userId={}", userId);
            throw new BadRequestException("Card type required");
        }

        CardType type;
        try {
            type = CardType.valueOf(dto.getType());
        } catch (Exception e) {
            log.error("Invalid card type | userId={} type={}", userId, dto.getType());
            throw new BadRequestException("Invalid card type");
        }

        // ========================= VALIDATIONS =========================

        List<CreditCard> userCards = repo.findAll()
                .stream()
                .filter(card -> card.getUserId().equals(userId))
                .toList();

        log.info("User has {} existing cards | userId={}", userCards.size(), userId);

        if (userCards.size() >= 2) {
            log.warn("Card limit exceeded | userId={}", userId);
            throw new BadRequestException("You cannot have more than 2 credit cards");
        }

        boolean alreadyExists = userCards.stream()
                .anyMatch(card -> card.getType() == type);

        if (alreadyExists) {
            log.warn("Duplicate card type request | userId={} type={}", userId, type);
            throw new BadRequestException("You already have a " + type + " card");
        }

        // ========================= CREATE CARD =========================

        CreditCard card = new CreditCard();
        card.setUserId(userId);
        card.setCardNumber(CardUtil.generateCardNumber());
        card.setType(type);
        card.setCreatedAt(LocalDateTime.now());
        card.setStatus(CardStatus.PENDING);

        switch (type) {
            case CLASSIC -> {
                card.setCreditLimit(50000.0);
                card.setInterestRate(3.5);
                card.setExpiryDate(LocalDate.now().plusYears(5));
            }
            case SILVER -> {
                card.setCreditLimit(100000.0);
                card.setInterestRate(2.5);
                card.setExpiryDate(LocalDate.now().plusYears(4));
            }
            case GOLD -> {
                card.setCreditLimit(200000.0);
                card.setInterestRate(1.5);
                card.setExpiryDate(LocalDate.now().plusYears(3));
            }
        }

        card.setAvailableLimit(card.getCreditLimit());
        card.setUsedLimit(0.0);

        CreditCard saved = repo.save(card);

        log.info("Card created successfully | cardId={} userId={} type={}",
                saved.getId(), userId, type);

        return saved;
    }

    public List<CreditCard> getUserCards(Long userId) {

        log.info("Fetching user cards | userId={}", userId);

        List<CreditCard> cards = repo.findAll()
                .stream()
                .filter(card -> card.getUserId().equals(userId))
                .toList();

        log.info("Total cards fetched | userId={} count={}", userId, cards.size());

        return cards;
    }

    // ========================= MANAGER =========================

    public List<CreditCard> getPendingCards() {

        log.info("Fetching pending cards");

        List<CreditCard> cards = repo.findAll()
                .stream()
                .filter(card -> card.getStatus() == CardStatus.PENDING)
                .toList();

        log.info("Pending cards count={}", cards.size());

        return cards;
    }

    public CreditCard approveCard(Long id) {

        log.info("Approving card | id={}", id);

        CreditCard card = getCardOrThrow(id);
        card.setStatus(CardStatus.ACTIVE);

        CreditCard saved = repo.save(card);

        log.info("Card approved | id={}", id);

        return saved;
    }

    public CreditCard rejectCard(Long id) {

        log.warn("Rejecting card | id={}", id);

        CreditCard card = getCardOrThrow(id);
        card.setStatus(CardStatus.CLOSED);

        CreditCard saved = repo.save(card);

        log.warn("Card rejected | id={}", id);

        return saved;
    }

    public List<CreditCard> getAllCards() {

        log.info("Fetching all cards");

        List<CreditCard> cards = repo.findAll();

        log.info("Total cards count={}", cards.size());

        return cards;
    }

    // ========================= ADMIN =========================

    public CreditCard blockCard(Long id) {

        log.warn("Blocking card | id={}", id);

        CreditCard card = getCardOrThrow(id);
        card.setStatus(CardStatus.BLOCKED);

        CreditCard saved = repo.save(card);

        log.warn("Card blocked | id={}", id);

        return saved;
    }

    public CreditCard unblockCard(Long id) {

        log.warn("Unblocking card | id={}", id);

        CreditCard card = getCardOrThrow(id);
        card.setStatus(CardStatus.ACTIVE);

        CreditCard saved = repo.save(card);

        log.info("Card unblocked | id={}", id);

        return saved;
    }

    public CreditCard updateLimit(Long id, Double newLimit) {

        log.info("Updating limit | id={} newLimit={}", id, newLimit);

        CreditCard card = getCardOrThrow(id);

        card.setCreditLimit(newLimit);
        card.setAvailableLimit(newLimit - card.getUsedLimit());

        CreditCard saved = repo.save(card);

        log.info("Limit updated | id={} newLimit={}", id, newLimit);

        return saved;
    }

    // ========================= COMMON =========================

    private CreditCard getCardOrThrow(Long id) {

        return repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Card not found | id={}", id);
                    return new ResourceNotFoundException("Card not found");
                });
    }

    public String setPin(Long userId, String cardNumber, PinRequestDTO dto) {

        log.info("Set PIN request | userId={} card={}", userId, maskCard(cardNumber));

        CreditCard card = repo.findByCardNumber(cardNumber)
                .orElseThrow(() -> {
                    log.error("Card not found for PIN set | card={}", maskCard(cardNumber));
                    return new ResourceNotFoundException("Card not found");
                });

        if (!card.getUserId().equals(userId)) {
            log.error("Unauthorized PIN attempt | userId={} card={}", userId, maskCard(cardNumber));
            throw new UnauthorizedException("Unauthorized access");
        }

        if (dto.getPin() == null || dto.getConfirmPin() == null) {
            log.error("PIN missing | userId={}", userId);
            throw new BadRequestException("PIN required");
        }

        if (!dto.getPin().equals(dto.getConfirmPin())) {
            log.error("PIN mismatch | userId={}", userId);
            throw new BadRequestException("PIN mismatch");
        }

        if (dto.getPin().length() != 4) {
            log.error("Invalid PIN length | userId={}", userId);
            throw new BadRequestException("PIN must be 4 digits");
        }

        card.setPin(dto.getPin());
        repo.save(card);

        log.info("PIN set successfully | userId={} card={}", userId, maskCard(cardNumber));

        return "PIN created successfully";
    }

    private String maskCard(String cardNumber) {
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}