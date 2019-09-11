package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Value
public final class AddAccountDto {
    public final String accountNumber;
    public final String accountName;
    public final String accountType;
    public final String currencyCode;

    @JsonCreator
    public AddAccountDto(@JsonProperty(value = "accountNumber") String accountNumber,
                         @JsonProperty(value = "accountName") String accountName,
                         @JsonProperty(value = "accountType") String accountType,
                         @JsonProperty(value = "currencyCode") String currencyCode) {
        this.accountNumber = accountNumber;
        this.accountName = accountName;
        this.accountType = accountType;
        this.currencyCode = currencyCode;
    }
}
