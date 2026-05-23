package com.bank.card.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bank.card.dto.*;
import com.bank.card.model.CreditCard;
import com.bank.card.service.CreditCardService;
import com.bank.card.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/credit-card")
@AllArgsConstructor
public class CreditCardController {

    private final CreditCardService service;

    // ================= USER =================

    @PostMapping("/user/request")
    public ResponseEntity<CreditCard> requestCard(
            HttpServletRequest request,
            @Valid @RequestBody CardRequestDTO dto) {

        Long userId = extractUserId(request);

        log.info("Card request initiated by userId: {} for type: {}", userId, dto.getType());

        return ResponseEntity.ok(service.requestCard(userId, dto));
    }

    @GetMapping("/user/cards")
    public ResponseEntity<List<CreditCard>> getUserCards(HttpServletRequest request) {

        Long userId = extractUserId(request);

        log.info("Fetching credit cards for userId: {}", userId);

        return ResponseEntity.ok(service.getUserCards(userId));
    }

    // ================= MANAGER =================

    @PostMapping("/manager/approve/{id}")
    public ResponseEntity<CreditCard> approveCard(@PathVariable Long id) {

        log.info("Manager approving cardId: {}", id);

        return ResponseEntity.ok(service.approveCard(id));
    }

    @PostMapping("/manager/reject/{id}")
    public ResponseEntity<CreditCard> rejectCard(@PathVariable Long id) {

        log.info("Manager rejecting cardId: {}", id);

        return ResponseEntity.ok(service.rejectCard(id));
    }

    // ================= ADMIN =================

    @PostMapping("/admin/block/{id}")
    public ResponseEntity<CreditCard> blockCard(@PathVariable Long id) {

        log.warn("Admin blocking cardId: {}", id);

        return ResponseEntity.ok(service.blockCard(id));
    }

    @PostMapping("/admin/unblock/{id}")
    public ResponseEntity<CreditCard> unblockCard(@PathVariable Long id) {

        log.info("Admin unblocking cardId: {}", id);

        return ResponseEntity.ok(service.unblockCard(id));
    }

    // ================= PIN =================

    @PostMapping("/user/set-pin/{cardNumber}")
    public ResponseEntity<String> setPin(
            HttpServletRequest request,
            @PathVariable String cardNumber,
            @Valid @RequestBody PinRequestDTO dto) {

        Long userId = extractUserId(request);

        log.info("PIN set request for card: {} by userId: {}", cardNumber, userId);

        return ResponseEntity.ok(service.setPin(userId, cardNumber, dto));
    }

    // ================= HEADER =================

    private Long extractUserId(HttpServletRequest request) {

        String userId = request.getHeader("X-User-Id");

        if (userId == null || userId.isBlank()) {
            log.error("Missing X-User-Id header");
            throw new UnauthorizedException("Unauthorized request");
        }

        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.error("Invalid X-User-Id format: {}", userId);
            throw new UnauthorizedException("Invalid User ID format");
        }
    }
}