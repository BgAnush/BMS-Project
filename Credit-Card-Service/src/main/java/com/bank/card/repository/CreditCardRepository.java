package com.bank.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.card.model.CreditCard;
import java.util.*;

public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    Optional<CreditCard> findByCardNumber(String cardNumber);
    List<CreditCard> findByStatus(String status);
	List<CreditCard> findByUserId(Long userId);
}