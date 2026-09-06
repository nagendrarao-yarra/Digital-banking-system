package com.BankingSystem.TransactionsServices.Respository;

import com.BankingSystem.TransactionsServices.Entities.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction,String > {
}
