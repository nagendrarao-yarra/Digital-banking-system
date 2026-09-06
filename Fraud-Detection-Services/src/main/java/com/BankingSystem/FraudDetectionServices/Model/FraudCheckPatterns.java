package com.BankingSystem.FraudDetectionServices.Model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class FraudCheckPatterns {
    private final RedisTemplate<String,String> redisTemplate;

    @Value("${fraudCheck.maxTransactionPerMinute}")
    private long maxTransactionPerMinute;

    @Value("${fraudCheck.suspiciousAmountMultiplier}")
    private double suspiciousAmountMultiplier;

    @Value("${fraudCheck.maxBalancePercentage}")
    private double maxBalancePercentage;
    public boolean isVelocityExceeded(String accountNumber)
    {
        String key = "fraud:velocity"+ accountNumber;
        Long count = redisTemplate.opsForValue().increment(key);
        //redis has many method like increment,decrement many more
        //this automatically save the key in the redis
//        First transaction:
//        Redis: 0 → 1
//        Second transaction:
//        Redis: 1 → 2
//        Third transaction:
//        Redis: 2 → 3
//        Fourth transaction:
//        Redis: 3 → 4

        if(count != null && count ==1)
        {
            redisTemplate.expire(key,60, TimeUnit.SECONDS);
        }

        log.info("Velocity check - account: {} count: {}/{}",
                accountNumber,count,maxTransactionPerMinute);

        return count != null && count > maxTransactionPerMinute;
    }
    public boolean isAmountSuspicious(
            String accountNumber,
            BigDecimal amount
    )
    {
        String avgkey = "fraud:avg_amount" +accountNumber;
        String avgstr = redisTemplate.opsForValue().get(avgkey);

        if(avgstr == null)
        {
            redisTemplate.opsForValue().set(avgkey,amount.toString());
            return false;
        }

        BigDecimal avgAmount = new BigDecimal(avgstr);
        BigDecimal threshold = avgAmount.multiply(BigDecimal.valueOf(suspiciousAmountMultiplier));

        BigDecimal newAvg = avgAmount.add(amount).divide(BigDecimal.valueOf(2),2, RoundingMode.HALF_UP);

        redisTemplate.opsForValue().set(avgkey,newAvg.toString());

        log.info("Amount check - amount :{} threshold: {} suspicious: {}",amount,threshold,amount.compareTo(threshold)>0);

        return amount.compareTo(threshold)>0;
    }

    public boolean isBalanceCheckFailed(BigDecimal senderBalance,BigDecimal amount)
    {
        BigDecimal maxAllowed = senderBalance.multiply(BigDecimal.valueOf(maxBalancePercentage));

        log.info("Balance check - amount: {} maxAllowed: {} suscpious:{}",amount,maxAllowed,amount.compareTo(maxAllowed)>0);

        return amount.compareTo(maxAllowed)>0;
    }

}
