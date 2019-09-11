package com.globalbank.bookentry.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventShards;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;

import java.math.BigDecimal;

/**
 * This interface defines all the events that the AccountEntity supports.
 * <p>
 * By convention, the events should be inner classes of the interface, which
 * makes it simple to get a complete picture of what events an entity has.
 */
public interface AccountEvent extends Jsonable, AggregateEvent<AccountEvent> {
    /**
     * Tags are used for getting and publishing streams of events. Each event
     * will have this tag, and in this case, we are partitioning the tags into
     * 4 shards, which means we can have 4 concurrent processors/publishers of
     * events.
     */
    AggregateEventShards<AccountEvent> TAG = AggregateEventTag.sharded(AccountEvent.class, 4);

    /**
     * An event that represents that a account has been added.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
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
            this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
            this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
        }
    }

    /**
     * An event that represents a change in the account details.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class AccountDetailsChanged implements AccountEvent {
        public final String accountNumber;
        public final String accountName;
        public final String accountType;
        public final String currencyCode;
        public final String timestamp;

        @JsonCreator
        public AccountDetailsChanged(@JsonProperty(value = "accountNumber") String accountNumber,
                            @JsonProperty(value = "accountName") String accountName,
                            @JsonProperty(value = "accountType") String accountType,
                            @JsonProperty(value = "currencyCode") String currencyCode,
                            @JsonProperty(value = "timestamp") String timestamp) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.accountName = Preconditions.checkNotNull(accountName, "accountName");
            this.accountType = Preconditions.checkNotNull(accountType, "accountType");
            this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
            this.timestamp = Preconditions.checkNotNull(timestamp, "timestamp");
        }
    }

    /**
     * An event that represents that a post has been added to an account
     */

    @Value
    @JsonDeserialize
    final class PostAdded implements AccountEvent {
        public final String accountNumber;
        public final String reference;
        public final String narrative;
        public final String currencyCode;
        public final BigDecimal amount;
        public final BigDecimal balance;
        public final String timestamp;

        @JsonCreator
        public PostAdded(@JsonProperty(value = "accountNumber") String accountNumber,
                         @JsonProperty(value = "reference") String reference,
                         @JsonProperty(value = "narrative") String narrative,
                         @JsonProperty(value = "currencyCode") String currencyCode,
                         @JsonProperty(value = "amount") BigDecimal amount,
                         @JsonProperty(value = "balance") BigDecimal balance,
                         @JsonProperty(value = "timestamp") String timestamp) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.reference = Preconditions.checkNotNull(reference, "reference");
            this.narrative = Preconditions.checkNotNull(narrative, "narrative");
            this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
            this.amount = amount;
            this.balance = balance;
            this.timestamp = timestamp;
        }
    }

    @Override
    default AggregateEventTagger<AccountEvent> aggregateTag() {
        return TAG;
    }
}
