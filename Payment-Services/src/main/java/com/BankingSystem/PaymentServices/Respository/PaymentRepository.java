package com.BankingSystem.PaymentServices.Respository;

import com.BankingSystem.PaymentServices.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, String> {
}
