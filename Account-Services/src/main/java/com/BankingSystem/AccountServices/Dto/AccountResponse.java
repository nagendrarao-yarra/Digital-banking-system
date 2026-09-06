package com.BankingSystem.AccountServices.Dto;
import com.BankingSystem.AccountServices.Entity.AccountStatus;
import com.BankingSystem.AccountServices.Entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
//it is like the response mean dto Responsenitty that
public class AccountResponse {

    private String id;

    private String accountNumber;

    private String accountHolderName;

    private String email;

    private String phone;

    private AccountType accountType;

    private AccountStatus status;

    private BigDecimal balance;

    private  BigDecimal dailyTransactionLimit;

    private LocalDateTime createdAt;
}
