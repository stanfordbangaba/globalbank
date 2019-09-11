package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class ReversalDto {
    public final String orgnlReference;
    public final String orgnlSourceAccount;
    public final String orgnlDestinationAccount;
    public final String orgnlCurrencyCode;
    public final BigDecimal orgnlAmount;

    @JsonCreator
    public ReversalDto(@JsonProperty(value = "orgnlReference") String orgnlReference,
                       @JsonProperty(value = "orgnlSourceAccount") String orgnlSourceAccount,
                       @JsonProperty(value = "orgnlDestinationAccount") String orgnlDestinationAccount,
                       @JsonProperty(value = "orgnlCurrencyCode") String orgnlCurrencyCode,
                       @JsonProperty(value = "orgnlAmount") BigDecimal orgnlAmount) {
        this.orgnlReference = orgnlReference;
        this.orgnlSourceAccount = orgnlSourceAccount;
        this.orgnlDestinationAccount = orgnlDestinationAccount;
        this.orgnlCurrencyCode = orgnlCurrencyCode;
        this.orgnlAmount = orgnlAmount;
    }
}
