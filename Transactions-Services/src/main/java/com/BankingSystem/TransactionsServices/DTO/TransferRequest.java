package com.BankingSystem.TransactionsServices.DTO;

import com.BankingSystem.TransactionsServices.Entities.TransactionStatus;
import com.BankingSystem.TransactionsServices.Entities.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransferRequest {

    @NotBlank(message = "Sender account number is required")
    private String senderAccountNumber;

    @NotBlank(message = "receiver account number is required")
    private String receiverAccountNumber;

    @NotNull(message = "Amt is required")
    @Positive(message = "Amt must be positive")
    private BigDecimal amount;

    private String description;


}
