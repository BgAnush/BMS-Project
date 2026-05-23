package com.bank.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.user.entity.BankUser;

public interface UserRepository extends JpaRepository<BankUser, Integer> {

	boolean existsByEmailId(String emailId);

	boolean existsByMobileNumber(String string);

	boolean existsByAccountNumber(Long accountNumber);

	boolean existsByMobileNumberAndProfileIdNot(String mobile, Integer id);

}