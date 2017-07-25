package com.revolut.core.service;

import com.revolut.core.model.AccountBalance;
import com.revolut.core.model.TransferRequest;

import java.math.BigDecimal;

public interface MoneyTransfer {

    void transfer(TransferRequest request);

    default void validateBalance(AccountBalance balance, BigDecimal sendAmount) {
        BigDecimal remainAmount;
        if ((remainAmount = balance.getMoneyAmount().subtract(sendAmount)).compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException(String.format("Can't send money. Reason: Insufficient funds %s", remainAmount));
        }
    }

}
