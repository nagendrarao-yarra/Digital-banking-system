package com.BankingSystem.TransactionsServices.Service;

import com.BankingSystem.TransactionsServices.Client.AccountServiceClient;
import com.BankingSystem.TransactionsServices.DTO.TransactionResponse;
import com.BankingSystem.TransactionsServices.DTO.TransferRequest;
import com.BankingSystem.TransactionsServices.Entities.Transaction;
import com.BankingSystem.TransactionsServices.Entities.TransactionStatus;
import com.BankingSystem.TransactionsServices.Entities.TransactionType;
import com.BankingSystem.TransactionsServices.Event.TransactionCompleteEvent;
import com.BankingSystem.TransactionsServices.Event.TransactionInitiatedEvent;
import com.BankingSystem.TransactionsServices.Mapper.TransactionMapper;
import com.BankingSystem.TransactionsServices.Respository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountServiceClient accountServiceClient;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    private final RedisTemplate<String,String> redisTemplate;

    private final KafkaTemplate<String,Object> kafkaTemplate;
    private static final String TRANSACTION_INITIATED_TOPIC = "transaction.initiated";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction.completed";
    private static final String FRAUD_DETECTED_TOPIC = "transaction.detected";
    private static final String TRANSACTION_REFUNDED_TOPIC = "transaction.refunded";

    /**
     * SAGA STEP 1-Intiated transfer
     * Deduct from sender via feign
     * Saves transaction as PROCESSING
     * Publish event to kafka for fraud check
     * return
     */

    public TransactionResponse transfer(TransferRequest request)
    {
        log.info("SAGA START - Transfer: {} -> {} amount: {}",request.getSenderAccountNumber(),request.getReceiverAccountNumber(),request.getAmount());
        //SAGA STEP 1 ->Deduct from the sender
        accountServiceClient.deductBalance(request.getSenderAccountNumber(), request.getAmount());
        Transaction  transaction = new Transaction();
        transaction.setSenderAccountNumber(request.getSenderAccountNumber());
        transaction.setReceiverAccountNumber(request.getReceiverAccountNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setDescription(request.getDescription());
        transaction.setReferenceNumber(UUID.randomUUID().toString());
        Transaction savedtransaction = transactionRepository.save(transaction);
        //here transaction just initiated
        //Now we need to publish kafka also TransactionInitiatedEvent
        //THIS IS SAGA STEP -2 -Publish for fraud check
        TransactionInitiatedEvent event = new TransactionInitiatedEvent(
                savedtransaction.getId(),
                savedtransaction.getSenderAccountNumber(),
                savedtransaction.getReceiverAccountNumber(),
                savedtransaction.getAmount(),
                savedtransaction.getDescription()
        );
        kafkaTemplate.send(TRANSACTION_INITIATED_TOPIC,savedtransaction.getId(),event);
        log.info("SAGA STEP 2 - TransactionInitiatedEvent published: {}",savedtransaction.getId());
        return transactionMapper.mapperTran(transaction);
    }

    public TransactionResponse getTransaction(String transactionId)
    {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(()->new RuntimeException("transaction Not Found"));
        return transactionMapper.mapperTran(transaction);
    }



    public TransactionResponse verifyOTP(String transactionId,String OTP) {
        log.info("OTP verification for the transaction: {}", transactionId);
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found " + transactionId));

        //actually see with what key you have stored in the redis
        //right in the TransactionEventConsumer class you would see
        //while generating otp.
        String otpkey = "verification:otp" + transactionId;
        String storedKey = redisTemplate.opsForValue().get(otpkey);

        if (storedKey == null) {
            //OTP already expired
            log.warn("OTP already expired for the transactionId: {}", transactionId);
            compensateTransaction(transaction, "OTP expired - transaction cancelled and amount refunded");
            return transactionMapper.mapperTran(transaction);
        }

        if (!storedKey.equals(OTP)) {
            //Block Account And Refund
            log.warn("Wrong OTP - blocking account and refunding: {}", transactionId);
            redisTemplate.delete(otpkey);
            blockAccountAndCompensate(transaction, "Wrong OTP entered - transaction cancelled, " +
                    "account blocked for security");
            return transactionMapper.mapperTran(transaction);
        }

        //OTP correct - complete transaction
        log.info("OTP verified - completing transaction: {}", transactionId);
        redisTemplate.delete(otpkey);
        completeTransaction(transaction);
        return transactionMapper.mapperTran(transaction);

    }

    private void compensateTransaction(Transaction transaction ,String reason)
    {
        log.warn("SAGA COMPENSATION -refunding: {} amount: {}",
                transaction.getSenderAccountNumber(),transaction.getAmount());

        //CREDIT MONEY BACK TO SENDER SYNCHRONOUSLY
        //CALLING THIS FROM THE ACCOUNT SERVICE CREDIT API
        //BY USING FEING
        accountServiceClient.creditBalance(transaction.getSenderAccountNumber(), transaction.getAmount());
        transaction.setStatus(TransactionStatus.FLAGGED);
        transaction.setFailureReason(reason + "-SAGA Compensation executed, amount refunded at "+ LocalDateTime.now());
        transactionRepository.save(transaction);

        //publish refund event - Notification service will alert user that
        //money has been received
        Map<String ,Object> refundEvent = new HashMap<>();
        refundEvent.put("transactionId",transaction.getId());
        refundEvent.put("senderAccountNumber",transaction.getSenderAccountNumber());
        refundEvent.put("amount",transaction.getAmount());
        refundEvent.put("reason",reason);

        kafkaTemplate.send(TRANSACTION_REFUNDED_TOPIC,transaction.getId(),refundEvent);

        log.info("SAGA COMPENSATION COMPLETE -{} refunded to {}",
                transaction.getAmount()
        ,transaction.getSenderAccountNumber());

    }

    private void blockAccountAndCompensate(Transaction transaction,String reason)
    {
        //Publish fraud.detected -> Account Service will block account
        Map<String,Object> fraudEvent = new HashMap<>();
        fraudEvent.put("transactionId",transaction.getId());
        fraudEvent.put("accountNumber",transaction.getSenderAccountNumber());
        fraudEvent.put("reason",reason);


        kafkaTemplate.send(FRAUD_DETECTED_TOPIC,transaction.getSenderAccountNumber(),fraudEvent);
        log.warn("fraud.detected published - account: {} will be blocked, Kindly contact to the bank",
                transaction.getSenderAccountNumber());
        //Accounteventconsumer we already consumed kafka .from that only we are blocking the account

        //SAGA COMPENSATION - refund Sender
        //Once blocked than we need to compensate right
        compensateTransaction(transaction,reason);
    }

    //seding money to the receiver
    private void  completeTransaction(Transaction transaction)
    {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        TransactionCompleteEvent completeEvent = new TransactionCompleteEvent(
               transaction.getId(),
               transaction.getSenderAccountNumber(),
                transaction.getReceiverAccountNumber(),
                transaction.getAmount(),
                transaction.getDescription()
        );

        kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC,transaction.getId(),completeEvent);

        log.info("SAGA COMPLETE -Transaction {} completed",transaction.getId());

    }


    public void processCleanResult(String transactionId)
    {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(() -> new RuntimeException("Transaction not found " + transactionId));

        if(transaction.getStatus()!=TransactionStatus.PROCESSING)
        {
            log.warn("Transaction {} not PROCESSING - skipping ",transactionId);
            return;
        }

        completeTransaction(transaction);

    }

}
