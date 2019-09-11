package com.globalbank.bookentry.stream.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;

@Value
public class PostStreamDto {
    private final String id;
    private final String accountNumber;
    private final String reference;
    private final String narrative;
    private final BigDecimal amount;
    private final BigDecimal balance;
    private final Instant dateCreated;

    @JsonCreator
    public PostStreamDto(String id, String accountNumber, String reference, String narrative, BigDecimal amount,
                         BigDecimal balance, Instant dateCreated) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.reference = reference;
        this.narrative = narrative;
        this.amount = amount;
        this.balance = balance;
        this.dateCreated = dateCreated;
    }
}
