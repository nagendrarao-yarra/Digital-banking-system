package com.BankingSystem.AccountServices.Repository;

import com.BankingSystem.AccountServices.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, String> {
    boolean existByEmail(String Email);
    boolean existByAccountNumber(String accountNumber);
    Optional<Account> findByAccountNumber(String accountNumber);
}
