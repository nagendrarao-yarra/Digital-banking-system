package com.BankingSystem.AccountServices.Service;

import com.BankingSystem.AccountServices.Dto.AccountResponse;
import com.BankingSystem.AccountServices.Dto.CreateAccountRequest;
import com.BankingSystem.AccountServices.Entity.Account;
import com.BankingSystem.AccountServices.Entity.AccountStatus;
import com.BankingSystem.AccountServices.Entity.AccountType;
import com.BankingSystem.AccountServices.Mapper.AccountMapper;
import com.BankingSystem.AccountServices.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final SecureRandom secureRandom;
    private final AccountMapper mapper;
    private final AccountRepository accountRepository;

    private KafkaTemplate<String, CreateAccountRequest> kafkaTemplate;

    public AccountResponse createAccount(CreateAccountRequest request)
    {
        log.info("Creating account for: {}",request.getEmail());

        if(accountRepository.existByEmail(request.getEmail()))
        {
            throw new RuntimeException("Account already exists for email: "+request.getEmail());
        }
        Account account= new Account();
        account.setAccountHolderName(request.getAccountHolderName());
        account.setEmail(request.getEmail());
        account.setPhone(request.getPhone());
        account.setAccountType(request.getAccountType());
        account.setStatus(AccountStatus.ACTIVE);
        account.setBalance(request.getInitialDeposit());
        account.setAccountNumber(generateAccount());
        account.setDailyTransactionLimit(
                request.getAccountType()== AccountType.SAVING
                ? new BigDecimal("100000"):new BigDecimal("500000")
        );
        Account saveAccount = accountRepository.save(account);
        log.info("Account Created :{}",saveAccount.getAccountNumber());

        return mapper.maptoaccount(saveAccount);
    }

//    Get Account by accountNumber
    public AccountResponse getAccount(String accountNo)
    {
        Account account = accountRepository.findByAccountNumber(accountNo).orElse(null);
        if(account == null)
        {
            throw  new RuntimeException("Account not Found");
        }
        return mapper.maptoaccount(account);
    }

    //getting Balance by account number
    public BigDecimal getBalance(String accountNo)
    {
        Account account = accountRepository.findByAccountNumber(accountNo).orElse(null);
        if(account == null)
        {
            throw  new RuntimeException("Account not Found");
        }
        return account.getBalance();
    }


    //Block account - called by fraud detection service via kafka

    public void blockAccount(String accountNo)
    {
        log.info("Blocking the account :{}",accountNo);
        Account account = accountRepository.findByAccountNumber(accountNo).orElse(null);
        if(account == null)
        {
            throw  new RuntimeException("Account not Found");
        }
        if(account.getStatus()!= AccountStatus.BLOCKED)
        {
            account.setStatus(AccountStatus.BLOCKED);
        }
        accountRepository.save(account);
        log.info("Account blocked:{}",accountNo);
    }


    //deduct balance from sender account and called by transaction service
    public void deductAmt(String accountNo,BigDecimal amt)
    {
        log.info("Deducting amt from :{}",accountNo);
        Account account = accountRepository.findByAccountNumber(accountNo).orElse(null);
        if(account == null)
        {
            throw  new RuntimeException("Account not Found");
        }
        if(account.getStatus()!=AccountStatus.ACTIVE)
        {
            throw new RuntimeException("Account is not active "+accountNo );
        }
        if(account.getBalance().compareTo(amt) < 0)
        {
            throw new RuntimeException("Insufficient balance"+accountNo);
        }
        account.setBalance(account.getBalance().subtract(amt));
        accountRepository.save(account);
        log.info("Balance Updated and New Balance:{}",account.getBalance());
    }

    //Credit balance called by transaction service via by kafka

    public void creditBalance(String accountNo,BigDecimal amt)
    {
        log.info("Crediting  amt to  :{}",accountNo);
        Account account = accountRepository.findByAccountNumber(accountNo).orElse(null);
        if(account == null)
        {
            throw  new RuntimeException("Account not Found");
        }
        if(account.getStatus()!=AccountStatus.ACTIVE)
        {
            throw new RuntimeException("Account is not active "+accountNo );
        }
        account.setBalance(account.getBalance().add(amt));
        accountRepository.save(account);
        log.info("Balance Credited and New Balance:{}",account.getBalance());
    }






























    //generate unique 12 digit account Number
    private String generateAccount()
    {
        String accountNumber;
        do{
            long number = secureRandom.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d",number);
        }while (accountRepository.existByAccountNumber(accountNumber));

        return accountNumber;
    }



}
