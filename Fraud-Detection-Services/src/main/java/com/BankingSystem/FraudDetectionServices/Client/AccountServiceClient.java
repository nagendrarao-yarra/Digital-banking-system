package com.BankingSystem.FraudDetectionServices.Client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "account-service",url = "${account.service.url}")
public interface AccountServiceClient {


    @GetMapping("/{accountNo}/balance")
     BigDecimal getBalance(
            @PathVariable String accountNo
    );
}
