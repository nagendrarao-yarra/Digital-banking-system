package com.BankingSystem.TransactionsServices.Controller;

import com.BankingSystem.TransactionsServices.DTO.TransactionResponse;
import com.BankingSystem.TransactionsServices.DTO.TransferRequest;
import com.BankingSystem.TransactionsServices.Respository.TransactionRepository;
import com.BankingSystem.TransactionsServices.Service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Slf4j
@RequiredArgsConstructor
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping()
    public ResponseEntity<TransactionRepository> transfer(
            @Valid @RequestBody TransferRequest request
            )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.transfer(request));

    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @PathVariable String transactionId
    )
    {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @GetMapping("/account/{userAccountNo}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable String userAccountNo
    )
    {
        return ResponseEntity.ok(transactionService.getTransactionHistory(userAccountNo));
    }


    //otp verification
    @PostMapping("/{transactionId}/verify")
    public ResponseEntity<TransactionResponse> verifyOTP(
            @PathVariable String transactionId,
            @RequestParam String otp
    )
    {
        log.info("OTP verification request - transaction :{}",transactionId);

        return ResponseEntity.ok(transactionService.verifyOTP(transactionId,otp));
    }


}
