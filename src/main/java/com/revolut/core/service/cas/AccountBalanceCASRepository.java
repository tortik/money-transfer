package com.revolut.core.service.cas;

import com.revolut.core.dao.AccountBalanceRepository;
import com.revolut.core.model.AccountBalance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;


public class AccountBalanceCASRepository implements AccountBalanceRepository<AtomicReference<AccountBalance>> {
    private ConcurrentHashMap<Long, AtomicReference<AccountBalance>> storage = new ConcurrentHashMap<>();

    public AtomicReference<AccountBalance> getBalance(long accNumber) {
        return storage.get(accNumber);
    }

    @Override
    public void setBalance(AtomicReference<AccountBalance> accountBalance) {
        storage.put(accountBalance.get().getAccNumber(), accountBalance);
    }
}
