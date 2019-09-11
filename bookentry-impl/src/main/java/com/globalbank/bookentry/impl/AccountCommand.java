package com.globalbank.bookentry.impl;

import akka.Done;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.globalbank.bookentry.api.ReadAccountDto;
import com.globalbank.bookentry.enums.CreditDebitIndicator;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.Value;
import com.globalbank.bookentry.pojo.PostingResponse;

import java.math.BigDecimal;

/**
 * This interface defines all the commands that the AccountEntity supports.
 * <p>
 * By convention, the commands should be inner classes of the interface, which
 * makes it simple to get a complete picture of what commands an entity
 * supports.
 */
public interface AccountCommand extends Jsonable {
    /**
     * A command to add an account.
     * <p>
     * It has a reply type of {@link akka.Done}, which is sent back to the caller
     * when all the events emitted by this command are successfully persisted.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class AddAccount implements AccountCommand, CompressedJsonable, PersistentEntity.ReplyType<Done> {
        public final String accountNumber;
        public final String accountName;
        public final String accountType;
        public final String currencyCode;

        @JsonCreator
        public AddAccount(@JsonProperty(value = "accountNumber") String accountNumber,
                          @JsonProperty(value = "accountName") String accountName,
                          @JsonProperty(value = "accountType") String accountType,
                          @JsonProperty(value = "currencyCode") String currencyCode) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.accountName = Preconditions.checkNotNull(accountName, "accountName");
            this.accountType = Preconditions.checkNotNull(accountType, "accountType");
            this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
        }
    }

    /**
     * A command to update account details.
     * <p>
     * The reply type is String, and will contain the update status
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class UpdateAccount implements AccountCommand, PersistentEntity.ReplyType<Done> {
        public final String accountNumber;
        public final String accountName;
        public final String accountType;

        @JsonCreator
        public UpdateAccount(@JsonProperty(value = "accountNumber") String accountNumber,
                          @JsonProperty(value = "accountName") String accountName,
                          @JsonProperty(value = "accountType") String accountType) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.accountName = Preconditions.checkNotNull(accountName, "accountName");
            this.accountType = Preconditions.checkNotNull(accountType, "accountType");
        }
    }

    /**
     * A command to update account details.
     * <p>
     * The reply type is String, and will contain the update status
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class ReadAccount implements AccountCommand, PersistentEntity.ReplyType<ReadAccountDto> {
        public final String accountNumber;

        @JsonCreator
        public ReadAccount(@JsonProperty(value = "accountNumber") String accountNumber) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
        }
    }

    /**
     * A command to get or initialize a suspense account.
     * <p>
     * The reply type is String, and will contain the update status
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class GetOrInitSuspenseAccount implements AccountCommand, PersistentEntity.ReplyType<ReadAccountDto> {
        public final String accountNumber;
        public final String accountName;
        public final String currencyCode;

        @JsonCreator
        public GetOrInitSuspenseAccount(@JsonProperty(value = "accountNumber") String accountNumber,
                                        @JsonProperty(value = "accountName") String accountName,
                                        @JsonProperty(value = "currencyCode") String currencyCode) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.accountName = Preconditions.checkNotNull(accountName, "accountName");
            this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
        }
    }

    /**
     * A command to add a post.
     * <p>
     * It has a reply type of {@link akka.Done}, which is sent back to the caller
     * when all the events emitted by this command are successfully persisted.
     */
    @SuppressWarnings("serial")
    @Value
    @JsonDeserialize
    final class AddPost implements AccountCommand, CompressedJsonable, PersistentEntity.ReplyType<PostingResponse> {
        public final String accountNumber;
        public final String reference;
        public final String narrative;
        public final String currencyCode;
        public final CreditDebitIndicator creditDebitIndicator;
        public final BigDecimal amount;

        @JsonCreator
        public AddPost(@JsonProperty(value = "accountNumber") String accountNumber,
                         @JsonProperty(value = "reference") String reference,
                         @JsonProperty(value = "narrative") String narrative,
                         @JsonProperty(value = "currencyCode") String currencyCode,
                         @JsonProperty(value = "creditDebitIndicator") CreditDebitIndicator creditDebitIndicator,
                         @JsonProperty(value = "amount") BigDecimal amount) {
            this.accountNumber = Preconditions.checkNotNull(accountNumber, "accountNumber");
            this.reference = Preconditions.checkNotNull(reference, "reference");
            this.narrative = Preconditions.checkNotNull(narrative, "narrative");
            this.currencyCode = Preconditions.checkNotNull(currencyCode, "currencyCode");
            this.creditDebitIndicator = Preconditions.checkNotNull(creditDebitIndicator, "creditDebitIndicator");
            this.amount = amount;
        }
    }
}
