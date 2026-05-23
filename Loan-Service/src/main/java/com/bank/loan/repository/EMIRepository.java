package com.bank.loan.repository;

import com.bank.loan.entity.EMI;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EMIRepository extends JpaRepository<EMI, Long> {

    List<EMI> findByLoanIdOrderByEmiNumberAsc(Long loanId);

    Optional<EMI> findByLoanIdAndEmiNumber(Long loanId, Integer emiNumber);

    List<EMI> findByLoanIdIn(List<Long> loanIds);

    List<EMI> findByLoanIdAndEmiNumberLessThan(Long loanId, Integer emiNumber);
}