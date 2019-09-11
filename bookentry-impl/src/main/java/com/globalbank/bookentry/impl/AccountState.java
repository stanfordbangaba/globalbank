package com.globalbank.bookentry.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.serialization.CompressedJsonable;
import lombok.Value;

import java.math.BigDecimal;

/**
 * The state for the {@link AccountEntity} entity.
 */
@SuppressWarnings("serial")
@Value
@JsonDeserialize
public final class AccountState implements CompressedJsonable {
    public final String accountNumber;
    public final String accountName;
    public final String accountType;
    public final String currencyCode;
    public final BigDecimal balance;
    public final String timestamp;

    @JsonCreator
    AccountState(@JsonProperty(value = "accountNumber") String accountNumber,
              @JsonProperty(value = "accountName") String accountName,
              @JsonProperty(value = "accountType") String accountType,
              @JsonProperty(value = "currencyCode") String currencyCode,
              @JsonProperty(value = "balance") BigDecimal balance,
              @JsonProperty(value = "timestamp") String timestamp) {
        this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
        this.accountName = Preconditions.checkNotNull(accountName, "accountName");
        this.accountType = Preconditions.checkNotNull(accountType, "accountType");
        this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
        this.balance = balance;
        this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
    }
}
