package com.BankingSystem.TransactionsServices.Mapper;

import com.BankingSystem.TransactionsServices.DTO.TransactionResponse;
import com.BankingSystem.TransactionsServices.Entities.Transaction;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse mapperTran(Transaction transaction);
}
