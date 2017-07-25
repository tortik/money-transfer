package com.revolut.core.dao;

import com.revolut.core.model.AccountBalance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccBalanceMapRepository implements AccountBalanceRepository<AccountBalance> {
    private Map<Long, AccountBalance> balances = new ConcurrentHashMap<>();


    @Override
    public AccountBalance getBalance(long account) {
        return balances.get(account);
    }

    @Override
    public void addNewBalance(AccountBalance accountBalance) {
        balances.putIfAbsent(accountBalance.getAccNumber(), accountBalance);
    }

    @Override
    public void setBalance(AccountBalance accountBalance) {
        balances.put(accountBalance.getAccNumber(), accountBalance);
    }
}
