package com.revolut.core.dao;


public interface AccountBalanceRepository<T> {

    T getBalance(long account);

    void addNewBalance(T accountBalance);

    void setBalance(T accountBalance);

}
