package com.BankingSystem.PaymentServices.Service;

import com.BankingSystem.PaymentServices.DTO.PaymentOrderResponse;
import com.BankingSystem.PaymentServices.DTO.PaymentRequest;
import com.BankingSystem.PaymentServices.Entity.Payment;
import com.BankingSystem.PaymentServices.Entity.PaymentStatus;
import com.BankingSystem.PaymentServices.Respository.PaymentRepository;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    private static final String PAYMENT_FAILED_TOPIC = "payment.failed";

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    public PaymentOrderResponse createPaymentOrder(PaymentRequest request) throws RazorpayException {
        log.info("Creating payment order for account: {} amount: {}",
                request.getAccountNumber(), request.getAmount());

        RazorpayClient razorpay = new RazorpayClient(keyId, keySecret);

        //Amount in paise
        int amountInPaise = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "rcpt_" +
                UUID.randomUUID().toString().replace("-", "").substring(0, 30));

        Order razorpayorder = razorpay.orders.create(orderRequest);
        log.info("Razorpay order created: {}", razorpayorder.get("id").toString());

        //save Payment record
        Payment payment = new Payment();
        payment.setRazorpayOrderId(request.getAccountNumber());
        payment.setAmount(request.getAmount());
        payment.setCurrency("INR");
        payment.setPaymentStatus(PaymentStatus.CREATED);
        payment.setDescription(request.getDescription());

        Payment save = paymentRepository.save(payment);

        return new PaymentOrderResponse(
                save.getId(),
                razorpayorder.get("Id").toString(),
                request.getAmount(),
                "INR",
                keyId,
                "CREATED"
        );
    }

    public void handleWebhook(Map<String, Object> payload)
    {
        
    }



}
