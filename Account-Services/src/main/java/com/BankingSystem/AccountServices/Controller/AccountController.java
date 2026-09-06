package com.BankingSystem.AccountServices.Controller;

import com.BankingSystem.AccountServices.Dto.AccountResponse;
import com.BankingSystem.AccountServices.Dto.CreateAccountRequest;
import com.BankingSystem.AccountServices.Repository.AccountRepository;
import com.BankingSystem.AccountServices.Service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController("/api/v1/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private AccountService accountService;
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestBody CreateAccountRequest createAccountRequest
            )
    {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.createAccount(createAccountRequest));
    }

    @GetMapping("/{accountNo}")
    public ResponseEntity<AccountResponse> getAccount(
            @PathVariable String accountNo
    )
    {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(accountService.getAccount(accountNo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{accountNo}/balance")
    public ResponseEntity<BigDecimal> getBalance(
            @PathVariable String accountNo
    )
    {
        return ResponseEntity.status(HttpStatus.OK).body(accountService.getBalance(accountNo));
    }

    /**
     * Block account - called by fraud detection service via kafka
     * @param accountNo
     * @return
     */
    @PutMapping("/{accountNo}/block")
    public ResponseEntity<String> blockAccount(
            @PathVariable String accountNo
    )
    {
        accountService.blockAccount(accountNo);
        return ResponseEntity.ok("Account Blocked Successfully");
    }

    //SAGA step 1
    //called by Transaction services when transfer is initiated

    @PutMapping("/{accountNumber}/deduct")
    public ResponseEntity<String> deductBalance(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount
    )
    {
        accountService.deductAmt(accountNumber,amount);
        return ResponseEntity.ok("Amount deducted Successfully");
    }

    //saga step 4 - compensating transaction endpoint
    //called by transaction service in two scenarios
    //1.Fraud detected -> refund sender(undo step 1)
    //2.Transaction completed -> Credit receiver

    @PutMapping("/{accountNumber}/credit")
    public ResponseEntity<String> creditBalance(
        @PathVariable String accountNumber,
        @RequestParam BigDecimal amount
    )
    {
        accountService.creditBalance(accountNumber,amount);
        return  ResponseEntity.ok("BALANCE CREDITED SUCCESSFULLY");
    }

}
