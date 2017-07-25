package com.revolut.core.dao;

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
    public void addNewBalance(AtomicReference<AccountBalance> accountBalance) {
        storage.putIfAbsent(accountBalance.get().getAccNumber(), accountBalance);
    }

    @Override
    public void setBalance(AtomicReference<AccountBalance> accountBalance) {
        storage.put(accountBalance.get().getAccNumber(), accountBalance);
    }
}
