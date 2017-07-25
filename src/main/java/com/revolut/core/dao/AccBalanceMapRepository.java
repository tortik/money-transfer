package com.revolut.core.dao;

import com.revolut.core.model.AccountBalance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by user on 7/24/17.
 */
public class AccBalanceMapRepository implements AccountBalanceRepository<AccountBalance> {
    private Map<Long, AccountBalance> balances = new ConcurrentHashMap<>();


    @Override
    public AccountBalance getBalance(long account) {
        return balances.get(account);
    }

    @Override
    public void setBalance(AccountBalance accountBalance) {
        balances.put(accountBalance.getAccNumber(), accountBalance);
    }
}
