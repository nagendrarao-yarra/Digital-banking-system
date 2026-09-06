package com.BankingSystem.PaymentServices.Controller;


import com.BankingSystem.PaymentServices.DTO.PaymentOrderResponse;
import com.BankingSystem.PaymentServices.DTO.PaymentRequest;
import com.BankingSystem.PaymentServices.Service.PaymentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping()
    public ResponseEntity<PaymentOrderResponse> createPaymentOrder(
            @Valid @RequestBody PaymentRequest request
            )
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPaymentOrder(request));
    }

    //razorpay webhook endpoint
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody Map<String,Object> payload
            )
    {
        paymentService.handleWebhook(payload);
        return ResponseEntity.ok("webhook processed");
    }
}
