package com.revolut.core.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;

import java.math.BigDecimal;


public class AccountBalance {

    private long accNumber;

    private BigDecimal moneyAmount = BigDecimal.ZERO;
    private BigDecimal blockedAmount = BigDecimal.ZERO;

    public AccountBalance() {
    }

    public void setAccNumber(long accNumber) {
        this.accNumber = accNumber;
    }

    public void setMoneyAmount(BigDecimal moneyAmount) {
        this.moneyAmount = moneyAmount;
    }

    public void setBlockedAmount(BigDecimal blockedAmount) {
        this.blockedAmount = blockedAmount;
    }


    public AccountBalance(long accNumber, BigDecimal moneyAmount,
                          BigDecimal blockedAmount) {
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
