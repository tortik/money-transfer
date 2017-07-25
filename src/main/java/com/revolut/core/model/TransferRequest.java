package com.revolut.core.model;



import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


public class TransferRequest {

    private long fromAcc;
    private long toAcc;
    private BigDecimal amount;
    private OffsetDateTime date;

    @JsonCreator
    private TransferRequest(@JsonProperty("fromAcc") long fromAcc, @JsonProperty("toAcc") long toAcc,
                            @JsonProperty("amount") BigDecimal amount) {
        this.fromAcc = fromAcc;
        this.toAcc = toAcc;
        this.amount = amount;
        this.date = OffsetDateTime.now();
    }

    public long getFromAcc() {
        return fromAcc;
    }

    public long getToAcc() {
        return toAcc;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "TransferRequest{" +
                "fromAcc=" + fromAcc +
                ", toAcc=" + toAcc +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}
