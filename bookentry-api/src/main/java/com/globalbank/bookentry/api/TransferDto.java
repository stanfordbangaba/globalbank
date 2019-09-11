package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.math.BigDecimal;

@Value
public class TransferDto {
    public final String reference;
    public final String sourceAccount;
    public final String destinationAccount;
    public final String currencyCode;
    public final BigDecimal amount;

    @JsonCreator
    public TransferDto(@JsonProperty(value = "reference") String reference,
                       @JsonProperty(value = "sourceAccount") String sourceAccount,
                       @JsonProperty(value = "destinationAccount") String destinationAccount,
                       @JsonProperty(value = "currencyCode") String currencyCode,
                       @JsonProperty(value = "amount") BigDecimal amount) {
        this.reference = reference;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.currencyCode = currencyCode;
        this.amount = amount;
    }
}
