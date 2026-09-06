package com.BankingSystem.AccountServices.Mapper;

import com.BankingSystem.AccountServices.Dto.AccountResponse;
import com.BankingSystem.AccountServices.Entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponse maptoaccount(Account account);
}
