package com.bank.creditcard;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.bank.card.dto.CardRequestDTO;
import com.bank.card.dto.PinRequestDTO;
import com.bank.card.enums.CardStatus;
import com.bank.card.exception.BadRequestException;
import com.bank.card.exception.ResourceNotFoundException;
import com.bank.card.exception.UnauthorizedException;
import com.bank.card.model.CreditCard;
import com.bank.card.repository.CreditCardRepository;
import com.bank.card.service.CreditCardService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceTest {

    @Mock
    private CreditCardRepository repo;

    @InjectMocks
    private CreditCardService service;

    @Test
    void requestCard_shouldCreateCard_success() {

        CardRequestDTO dto = new CardRequestDTO();
        dto.setType("GOLD");

        when(repo.save(any(CreditCard.class)))
                .thenAnswer(i -> i.getArgument(0));

        CreditCard result = service.requestCard(1L, dto);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(CardStatus.PENDING, result.getStatus());
    }

    @Test
    void getUserCards_shouldReturnFilteredCards() {

        CreditCard card1 = new CreditCard();
        card1.setUserId(1L);

        CreditCard card2 = new CreditCard();
        card2.setUserId(2L);

        when(repo.findAll()).thenReturn(List.of(card1, card2));

        List<CreditCard> result = service.getUserCards(1L);

        assertEquals(1, result.size());
    }

    @Test
    void approveCard_shouldActivateCard() {

        CreditCard card = new CreditCard();
        card.setId(1L);
        card.setStatus(CardStatus.PENDING);

        when(repo.findById(1L)).thenReturn(Optional.of(card));
        when(repo.save(any())).thenReturn(card);

        CreditCard result = service.approveCard(1L);

        assertEquals(CardStatus.ACTIVE, result.getStatus());
    }
    @Test
    void requestCard_classic_success() {

        CardRequestDTO dto = new CardRequestDTO();
        dto.setType("CLASSIC");

        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        CreditCard card = service.requestCard(1L, dto);

        assertEquals(CardStatus.PENDING, card.getStatus());
        assertEquals(50000.0, card.getCreditLimit());
    }
    @Test
    void requestCard_silver_success() {

        CardRequestDTO dto = new CardRequestDTO();
        dto.setType("SILVER");

        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        CreditCard card = service.requestCard(1L, dto);

        assertEquals(100000.0, card.getCreditLimit());
    }
    @Test
    void requestCard_gold_success() {

        CardRequestDTO dto = new CardRequestDTO();
        dto.setType("GOLD");

        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        CreditCard card = service.requestCard(1L, dto);

        assertEquals(200000.0, card.getCreditLimit());
    }
    @Test
    void getUserCards_success() {

        CreditCard c1 = new CreditCard();
        c1.setUserId(1L);

        CreditCard c2 = new CreditCard();
        c2.setUserId(2L);

        when(repo.findAll()).thenReturn(List.of(c1, c2));

        List<CreditCard> result = service.getUserCards(1L);

        assertEquals(1, result.size());
    }
    @Test
    void getPendingCards_success() {

        CreditCard c1 = new CreditCard();
        c1.setStatus(CardStatus.PENDING);

        CreditCard c2 = new CreditCard();
        c2.setStatus(CardStatus.ACTIVE);

        when(repo.findAll()).thenReturn(List.of(c1, c2));

        List<CreditCard> result = service.getPendingCards();

        assertEquals(1, result.size());
    }
    @Test
    void approveCard_success() {

        CreditCard card = new CreditCard();
        card.setId(1L);
        card.setStatus(CardStatus.PENDING);

        when(repo.findById(1L)).thenReturn(Optional.of(card));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        CreditCard result = service.approveCard(1L);

        assertEquals(CardStatus.ACTIVE, result.getStatus());
    }
    @Test
    void rejectCard_success() {

        CreditCard card = new CreditCard();
        card.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(card));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        CreditCard result = service.rejectCard(1L);

        assertEquals(CardStatus.CLOSED, result.getStatus());
    }
    @Test
    void block_unblock_success() {

        CreditCard card = new CreditCard();
        card.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(card));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        CreditCard blocked = service.blockCard(1L);
        assertEquals(CardStatus.BLOCKED, blocked.getStatus());

        CreditCard unblocked = service.unblockCard(1L);
        assertEquals(CardStatus.ACTIVE, unblocked.getStatus());
    }
    @Test
    void updateLimit_success() {

        CreditCard card = new CreditCard();
        card.setId(1L);
        card.setUsedLimit(200.0);

        when(repo.findById(1L)).thenReturn(Optional.of(card));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        CreditCard result = service.updateLimit(1L, 1000.0);

        assertEquals(1000.0, result.getCreditLimit());
        assertEquals(800.0, result.getAvailableLimit());
    }
    @Test
    void getCardOrThrow_notFound() {

        when(repo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.approveCard(1L));
    }
    @Test
    void setPin_success() {

        CreditCard card = new CreditCard();
        card.setUserId(1L);
        card.setCardNumber("1234");

        PinRequestDTO dto = new PinRequestDTO();
        dto.setPin("1234");
        dto.setConfirmPin("1234");

        when(repo.findByCardNumber("1234")).thenReturn(Optional.of(card));
        when(repo.save(any())).thenReturn(card);

        String result = service.setPin(1L, "1234", dto);

        assertEquals("PIN created successfully", result);
    }
    @Test
    void setPin_mismatch() {

        CreditCard card = new CreditCard();
        card.setUserId(1L);
        card.setCardNumber("1234");

        PinRequestDTO dto = new PinRequestDTO();
        dto.setPin("1234");
        dto.setConfirmPin("9999");

        when(repo.findByCardNumber("1234")).thenReturn(Optional.of(card));

        assertThrows(BadRequestException.class,
                () -> service.setPin(1L, "1234", dto));
    }
    @Test
    void setPin_unauthorized() {

        CreditCard card = new CreditCard();
        card.setUserId(2L);
        card.setCardNumber("1234");

        PinRequestDTO dto = new PinRequestDTO();
        dto.setPin("1234");
        dto.setConfirmPin("1234");

        when(repo.findByCardNumber("1234")).thenReturn(Optional.of(card));

        assertThrows(UnauthorizedException.class,
                () -> service.setPin(1L, "1234", dto));
    }
    @Test
    void setPin_shouldThrowBadRequestException_whenPinsDoNotMatch() {

        // =========================================================
        // GIVEN
        // =========================================================
        Long userId = 1L;
        String cardNumber = "1234567812345678";

        CreditCard card = new CreditCard();
        card.setUserId(userId);
        card.setCardNumber(cardNumber);

        PinRequestDTO dto = new PinRequestDTO();
        dto.setPin("1234");
        dto.setConfirmPin("0000");

        when(repo.findByCardNumber(cardNumber))
                .thenReturn(Optional.of(card));

        // =========================================================
        // WHEN + THEN
        // =========================================================
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.setPin(userId, cardNumber, dto)
        );

        assertEquals("PIN mismatch", exception.getMessage());

        verify(repo, times(1))
                .findByCardNumber(cardNumber);

        verify(repo, never())
                .save(any(CreditCard.class));
    }
}