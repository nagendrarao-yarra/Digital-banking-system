package com.BankingSystem.TransactionsServices.DTO;

import com.BankingSystem.TransactionsServices.Entities.TransactionStatus;
import com.BankingSystem.TransactionsServices.Entities.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransactionResponse {

    private String id;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private TransactionType type;

    private TransactionStatus status;

    private String description;

    private String failureReason;

    private String referenceNumber;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
}
