package com.BankingSystem.TransactionsServices.Entities;


/**
 * Transaction lifeCycle flow
 *
 * PENDING -> PROCESSING -> COMPLETED (clean transaction)
 * basically completed means money credited to receiver
 *
 * Next process
 * PENDING -> PROCESSING -> PENDING_VERIFICATION(Suspicious detected)
 * if suspicious detected then bank need to verify with the user
 * if the user verified the -> COMPLETED(verified like otp,phone call etc) and
 * if not verified then we ->FLAGGED(saga refund) and block the account
 * PENDING -> PROCESSING FAILED -> FLAGGED
 *
 */


public enum TransactionStatus {

    PENDING,
    PROCESSING,
    PENDING_VERIFICATION,
    COMPLETED,
    FAILED,
    FLAGGED

}
