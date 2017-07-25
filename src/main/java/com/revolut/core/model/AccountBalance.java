package com.revolut.core.model;


import java.math.BigDecimal;

public class AccountBalance {

    private long accNumber;
    private BigDecimal moneyAmount = BigDecimal.ZERO;
    private BigDecimal blockedAmount = BigDecimal.ZERO;

    public AccountBalance(long accNumber, BigDecimal moneyAmount, BigDecimal blockedAmount) {
        this.accNumber = accNumber;
        this.moneyAmount = moneyAmount;
        this.blockedAmount = blockedAmount;
    }

    public long getAccNumber() {
        return accNumber;
    }

    public BigDecimal getMoneyAmount() {
        return moneyAmount;
    }

    public BigDecimal getBlockedAmount() {
        return blockedAmount;
    }

    @Override
    public String toString() {
        return "AccountBalance{" +
                "accNumber=" + accNumber +
                ", moneyAmount=" + moneyAmount +
                ", blockedAmount=" + blockedAmount +
                '}';
    }
}
