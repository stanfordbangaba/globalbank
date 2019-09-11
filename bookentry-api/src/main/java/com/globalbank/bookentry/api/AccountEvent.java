package com.globalbank.bookentry.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import lombok.Value;

import java.math.BigDecimal;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AccountEvent.AccountAdded.class, name = "account-added"),
        @JsonSubTypes.Type(value = AccountEvent.AccountDetailsChanged.class, name = "account-details-changed"),
        @JsonSubTypes.Type(value = AccountEvent.PostAdded.class, name = "post-added")
})
public interface AccountEvent {
    String getAccountNumber();

    @Value
    final class AccountAdded implements AccountEvent {
        public final String accountNumber;
        public final String accountName;
        public final String accountType;
        public final String currencyCode;
        public final String timestamp;

        @JsonCreator
        public AccountAdded(@JsonProperty(value = "accountNumber") String accountNumber,
                         @JsonProperty(value = "accountName") String accountName,
                         @JsonProperty(value = "accountType") String accountType,
                         @JsonProperty(value = "currencyCode") String currencyCode,
                         @JsonProperty(value = "timestamp") String timestamp) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.accountName = Preconditions.checkNotNull(accountName, "accountName");
            this.accountType = Preconditions.checkNotNull(accountType, "accountType");
            //this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
            this.currencyCode = currencyCode;
            this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
        }
    }

    @Value
    final class AccountDetailsChanged implements AccountEvent {
        public final String accountNumber;
        public final String accountName;
        public final String accountType;
        public final String timestamp;

        @JsonCreator
        public AccountDetailsChanged(String accountNumber, String accountName, String accountType, String timestamp) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.accountName = Preconditions.checkNotNull(accountName, "accountName");
            //this.accountType = Preconditions.checkNotNull(accountType, "accountType");
            this.accountType = accountType;
            this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
        }
    }

    @Value
    final class PostAdded implements AccountEvent {
        public final String accountNumber;
        public final String reference;
        public final String narrative;
        public final BigDecimal amount;
        public final BigDecimal balance;
        public final String timestamp;

        @JsonCreator
        public PostAdded(@JsonProperty(value = "accountNumber") String accountNumber,
                         @JsonProperty(value = "reference") String reference,
                         @JsonProperty(value = "narrative") String narrative,
                         @JsonProperty(value = "amount") BigDecimal amount,
                         @JsonProperty(value = "balance") BigDecimal balance,
                         @JsonProperty(value = "timestamp") String timestamp) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.reference = Preconditions.checkNotNull(reference, "reference");
            this.narrative = Preconditions.checkNotNull(narrative, "narrative");
            this.amount = amount;
            this.balance = balance;
            this.timestamp = timestamp;
        }
    }
}
