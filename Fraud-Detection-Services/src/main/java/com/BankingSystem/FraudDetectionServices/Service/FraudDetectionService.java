package com.BankingSystem.FraudDetectionServices.Service;

import com.BankingSystem.FraudDetectionServices.Client.AccountServiceClient;
import com.BankingSystem.FraudDetectionServices.Model.FraudCheck;
import com.BankingSystem.FraudDetectionServices.Model.FraudCheckResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class FraudDetectionService {
    private final KafkaTemplate<String,Object> kafkaTemplate;
    private AccountServiceClient accountServiceClient;
    private FraudCheck fraudCheck;


    private static final String VERIFICATION_REQUIRED_TOPIC = "verification.required";
    private static final String FRAUD_CHECK_CLEAN_RESULT_TOPIC = "fraud.check.clean";

    public void checkTransaction(Map<String,Object> payload)
    {
        String transactionId = (String)payload.get("transactionId");
        String accountNumber = (String)payload.get("senderAccountNumber");
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());

        BigDecimal senderBalance = accountServiceClient.getBalance(accountNumber);

        log.info("Checking transaction: {} account: {} amount: {} balance: {} ",transactionId,accountNumber,amount,senderBalance);

        FraudCheckResult result =  fraudCheck.performFraudChecks(accountNumber,amount,senderBalance );

        if(result.isFraud()) {
            //tho humme habhi fraud detect hua hai tho max humme otp verification ko jana padega

            log.info("Suspicious activity detected - account {}" +
                    "reason: {} -requestion OTP verification", accountNumber, result.getReason());

            Map<String, Object> verificationEvent = new HashMap<>();

            verificationEvent.put("transactionId", transactionId);
            verificationEvent.put("accountNumber", accountNumber);
            verificationEvent.put("amount", amount);
            verificationEvent.put("reason", result.getReason());

            kafkaTemplate.send(VERIFICATION_REQUIRED_TOPIC, transactionId, verificationEvent);
            //now this kafka is going to consume by the transactioneventconsumer;
        }
        else{
            log.info("Transaction clean");
            Map<String, Object> transactionCleanEvent = new HashMap<>();
            transactionCleanEvent.put("transactionId", transactionId);
            transactionCleanEvent.put("isFraud", false);
            transactionCleanEvent.put("reason", null);

            //This one is consumed in the transaction event consumer
            kafkaTemplate.send(FRAUD_CHECK_CLEAN_RESULT_TOPIC, transactionId, transactionCleanEvent);
        }


        }
    }
