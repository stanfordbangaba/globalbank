package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;

@Value
@JsonDeserialize
public class ReadAccountDto {
    public final String accountNumber;
    public final String accountName;
    public final String accountType;
    public final String currencyCode;
    public final BigDecimal balance;
    public final String timestamp;

    @JsonCreator
    public ReadAccountDto(@JsonProperty(value = "accountNumber") String accountNumber,
                          @JsonProperty(value = "accountName") String accountName,
                          @JsonProperty(value = "accountType") String accountType,
                          @JsonProperty(value = "currencyCode") String currencyCode,
                          @JsonProperty(value = "balance") BigDecimal balance,
                          @JsonProperty(value = "timestamp") String timestamp) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountType = accountType;
        this.currencyCode = currencyCode;
        this.balance = balance;
        this.timestamp = timestamp;
    }
}
