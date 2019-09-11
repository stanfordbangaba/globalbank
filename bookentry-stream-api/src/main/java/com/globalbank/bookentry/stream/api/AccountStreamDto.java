package com.globalbank.bookentry.stream.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Value;

import java.time.Instant;

@Value
public class AccountStreamDto {

    private final String accountNumber;
    private final String accountName;
    private final String accountType;
    private final String currencyCode;
    private final Instant dateCreated;

    @JsonCreator
    public AccountStreamDto(String accountNumber, String accountName, String accountType, String currencyCode, Instant dateCreated) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountType = accountType;
        this.currencyCode = currencyCode;
        this.dateCreated = dateCreated;
    }
}
