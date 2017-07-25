package com.revolut.core.dao;



public interface AccountBalanceRepository<T> {

    T getBalance(long account);

    void setBalance(T accountBalance);

}
