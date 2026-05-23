package com.bank.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.bank.auth.entity.AuthUser;

public interface AuthRepository extends JpaRepository<AuthUser, Integer> {
	Optional<AuthUser> findByCustomerId(String customerId);

	Optional<AuthUser> findByEmail(String email);
}