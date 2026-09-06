package com.BankingSystem.FraudDetectionServices.Model;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Service
public class FraudCheck {

    private final FraudCheckPatterns fraudCheckPatterns;

    public FraudCheckResult performFraudChecks(
            String accountNumber,
            BigDecimal amount,
            BigDecimal senderAmount
    )
    {
        //Pattern 1: Velocity Check
        if(fraudCheckPatterns.isVelocityExceeded(accountNumber))
        {
            return new FraudCheckResult(true,"Too may transactions in 60 seconds "+ "-Velocity limit exceeded");
        }

        //Pattern 2:Amount Check
        if(fraudCheckPatterns.isAmountSuspicious(accountNumber,amount))
        {
            return  new FraudCheckResult(
                    true,"Unusual transaction amount "+
                    " - exceeds 3x your average");
        }

        //Pattern 3:Balance Check
        if(senderAmount.compareTo(BigDecimal.ZERO)>0 && fraudCheckPatterns.isBalanceCheckFailed(senderAmount,amount)) {
            return new FraudCheckResult(true, "Transaction exceed 90% of account balance");
        }
        return new FraudCheckResult(false,null);
    }

}
