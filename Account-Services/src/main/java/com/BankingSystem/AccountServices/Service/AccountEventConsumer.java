package com.BankingSystem.AccountServices.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountEventConsumer {

    //Consumer Transaction.completed event from kafka
    //credit receiver account
    private final AccountService accountService;
    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(
            @Payload Map<String,Object> payload
            //for now we had taken the generic map if you want
            //becuase for now i don't know the event name
            )
    {
        try{
            String receiverAccount = (String) payload.get("receiverAccountNumber");
            BigDecimal amt = new BigDecimal(payload.get("amount").toString());

            log.info("Crediting account: {} amount:{}",receiverAccount,amt);
            accountService.creditBalance(receiverAccount,amt);
        }
        catch (Exception e)
        {
            log.error("Error crediting account: {}",e.getMessage());
        }
    }


    //consume fraud.detected event from kafka

    public void consumeFraudDetected(
            @Payload Map<String,Object> payload
    )
    {
        try{
            String accountNumber = (String)payload.get("accountNumber");
            log.info("Fraud detected - blocking account:{}",accountNumber);
            accountService.blockAccount(accountNumber);
        } catch (Exception e) {
            log.error("Error blocking account :{}",e.getMessage());
        }
    }
}
