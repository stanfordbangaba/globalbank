package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class DepositDto {
    public final String reference;
    public final String accountNumber;
    public final String currencyCode;
    public final BigDecimal amount;

    @JsonCreator
    public DepositDto(@JsonProperty(value = "reference") String reference,
                      @JsonProperty(value = "accountNumber") String accountNumber,
                      @JsonProperty(value = "currencyCode") String currencyCode,
                      @JsonProperty(value = "amount") BigDecimal amount) {
        this.reference = reference;
        this.accountNumber = accountNumber;
        this.currencyCode = currencyCode;
        this.amount = amount;
    }
}
