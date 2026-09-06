package com.BankingSystem.AccountServices.Dto;
import com.BankingSystem.AccountServices.Entity.AccountType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
//it is like a request body
public class CreateAccountRequest {

    @NotBlank(message = "Account Holder name required")
    private String accountHolderName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Phone is required")
    private AccountType accountType;

    @NotBlank(message = "Initial deposit is required")
    @Positive(message = "Initial Deposit must be positive")
    private BigDecimal initialDeposit;
}
