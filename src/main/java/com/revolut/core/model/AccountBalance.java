package com.revolut.core.model;



import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;


public class AccountBalance {

    private long accNumber;

    private BigDecimal moneyAmount = BigDecimal.ZERO;
    private BigDecimal blockedAmount = BigDecimal.ZERO;

    @JsonCreator
    public AccountBalance(@JsonProperty("accNumber") long accNumber, @JsonProperty("moneyAmount") BigDecimal moneyAmount,
                          @JsonProperty("blockedAmount") BigDecimal blockedAmount) {
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
