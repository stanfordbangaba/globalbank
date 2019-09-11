package com.globalbank.bookentry.impl;

import akka.Done;
import akka.NotUsed;
import akka.japi.Pair;
import com.globalbank.bookentry.api.*;
import com.globalbank.bookentry.util.ValidationUtil;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.broker.TopicProducer;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import com.globalbank.bookentry.api.*;
import com.globalbank.bookentry.enums.CreditDebitIndicator;
import com.globalbank.bookentry.impl.AccountCommand.*;
import com.globalbank.bookentry.init.AccountInitializer;
import com.globalbank.bookentry.pojo.PostingRequest;
import com.globalbank.bookentry.util.Constants;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.*;

/**
 * Implementation of the BookentryService.
 */

@Slf4j
public class BookentryServiceImpl implements BookentryService {

    private final PersistentEntityRegistry persistentEntityRegistry;
    private final AccountInitializer accountInitializer;

    @Inject
    public BookentryServiceImpl(PersistentEntityRegistry persistentEntityRegistry, AccountInitializer accountInitializer) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        this.accountInitializer = accountInitializer;

        persistentEntityRegistry.register(AccountEntity.class);
    }

    @Override
    public ServiceCall<AddAccountDto, Done> addAccount() {
        return request -> {
            // Look up the account entity for the given accountNumber.
            PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, request.accountNumber);
            // Tell the entity to use the account info specified.
            return ref.ask(new AddAccount(request.accountNumber, request.accountName, request.accountType, request.currencyCode));
        };
    }

    @Override
    public ServiceCall<UpdateAccountDto, Done> updateAccount() {
        return request -> {
            // Look up the account entity for the given accountNumber.
            PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, request.accountNumber);
            // Tell the entity to update itself with provided details.
            return ref.ask(new UpdateAccount(request.accountNumber, request.accountName, request.accountType));
        };
    }

    @Override
    public ServiceCall<NotUsed, ReadAccountDto> readAccount(String accountNumber) {
        return request -> {
            // Look up the account entity for the given ID.
            PersistentEntityRef<AccountCommand> ref = persistentEntityRegistry.refFor(AccountEntity.class, accountNumber);
            // Ask the entity the ReadAccount command.
            return ref.ask(new ReadAccount(accountNumber));
        };
    }

    @Override
    public ServiceCall<DepositDto, ServiceResponse> performDeposit() {
        return request -> {
            try {
                log.info("Performing deposit : Account {}", request.accountNumber);
                log.info("Deposit Amt : {} {}", request.currencyCode, request.amount);

                //reject if amount is less than or equal to zero
                if (!ValidationUtil.isAmountValid(request.amount)) {
                    log.info("Rejecting deposit with invalid amount");
                    return completedFuture(new ServiceResponse(Constants.RC_INVALID_AMOUNT, "Invalid amount"));
                }

                // Look up the account entities for the given account numbers and post.
                String sourceAccountNr = Constants.DEPOSIT_CASH_ACCOUNT_PREFIX.concat(request.currencyCode);
                String sourceAccountNm = "DEPOSIT CASH SUSPENSE " + request.currencyCode;

                return accountInitializer.getSuspenseAccountEntityRef(sourceAccountNr, sourceAccountNm, request.currencyCode)
                        .thenComposeAsync(sourceAccountRef -> {
                            PersistentEntityRef<AccountCommand> destAccountRef = persistentEntityRegistry
                                    .refFor(AccountEntity.class, request.accountNumber);

                            return this.doPosting(sourceAccountRef, destAccountRef,
                                    new PostingRequest(request.reference, sourceAccountNr, request.accountNumber,
                                            "Cash Deposit", request.currencyCode, request.amount));
                        });

            } catch (Exception e) {
                log.error("Exception occurred performing deposit : {}", e.getMessage());
                e.printStackTrace();

                return completedFuture(ServiceResponse.of(Constants.RC_GENERAL_ERROR, "General error"));
            }
        };
    }

    @Override
    public ServiceCall<TransferDto, ServiceResponse> performTransfer() {
        return request -> {
            try {
                log.info("Performing transfer : Src {} Dest {}", request.sourceAccount, request.destinationAccount);
                log.info("Transfer Amt : {} {}", request.currencyCode, request.amount);

                //reject if amount is less than or equal to zero
                if (!ValidationUtil.isAmountValid(request.amount)) {
                    log.info("Rejecting transfer with invalid amount");
                    return completedFuture(ServiceResponse.of(Constants.RC_INVALID_AMOUNT, "Invalid amount"));
                }

                // Look up the account entities for the given account numbers.
                PersistentEntityRef<AccountCommand> sourceAccountRef = persistentEntityRegistry
                        .refFor(AccountEntity.class, request.sourceAccount);

                PersistentEntityRef<AccountCommand> destAccountRef = persistentEntityRegistry
                        .refFor(AccountEntity.class, request.destinationAccount);

                return this.doPosting(sourceAccountRef, destAccountRef,
                        new PostingRequest(request.reference, request.sourceAccount, request.destinationAccount,
                                "Transfer", request.currencyCode, request.amount));
            } catch (Exception e) {
                log.error("Exception occurred performing transfer : {}", e.getMessage());
                e.printStackTrace();

                return completedFuture(ServiceResponse.of(Constants.RC_GENERAL_ERROR, "General error"));
            }
        };
    }

    @Override
    public ServiceCall<ReversalDto, ServiceResponse> performReversal() {
        return request -> {
            try {
                log.info("Performing reversal : OrgnlSrc {} OrgnlDest {}", request.orgnlSourceAccount, request.orgnlDestinationAccount);
                log.info("Reversal Amt : {} {}", request.orgnlCurrencyCode, request.orgnlAmount);

                //reject if amount is less than or equal to zero
                if (!ValidationUtil.isAmountValid(request.orgnlAmount)) {
                    log.info("Rejecting reversal with invalid amount");
                    return completedFuture(ServiceResponse.of(Constants.RC_INVALID_AMOUNT, "Invalid amount"));
                }

                // Look up the account entities for the given account numbers.
                PersistentEntityRef<AccountCommand> sourceAccountRef = persistentEntityRegistry
                        .refFor(AccountEntity.class, request.orgnlSourceAccount);

                PersistentEntityRef<AccountCommand> destAccountRef = persistentEntityRegistry
                        .refFor(AccountEntity.class, request.orgnlDestinationAccount);

                //switch source and destination accounts
                //more validation may be needed to validate original postings to be reversed
                return this.doPosting(sourceAccountRef, destAccountRef,
                        new PostingRequest(request.orgnlReference, request.orgnlDestinationAccount, request.orgnlSourceAccount,
                                "Reversal", request.orgnlCurrencyCode, request.orgnlAmount));
            } catch (Exception e) {
                log.error("Exception occurred performing reversal : {}", e.getMessage());
                e.printStackTrace();

                return completedFuture(ServiceResponse.of(Constants.RC_GENERAL_ERROR, "General error"));
            }
        };
    }

    private CompletionStage<ServiceResponse> doPosting(PersistentEntityRef<AccountCommand> sourceAccountRef,
                                                       PersistentEntityRef<AccountCommand> destAccountRef,
                                                       PostingRequest postingRequest) {

        log.info("Source Acc Entity : {}", sourceAccountRef);
        log.info("Dest Acc Entity : {}", destAccountRef);

        //Let's make sure we do atomic postings and rollbacks here
        //We cannot use transactional in distributed messaging context, using saga to ensure atomicity
        return sourceAccountRef.withAskTimeout(Duration.ofSeconds(5))
                .ask(new AddPost(postingRequest.sourceAccount, postingRequest.reference, postingRequest.narrative,
                        postingRequest.currencyCode, CreditDebitIndicator.Debit, postingRequest.amount))
                .thenComposeAsync(sourcePostingResponse -> {
                    log.info("Source Acc Resp : {}", sourcePostingResponse);

                    //if debit successful, proceed with credit
                    if (Constants.RC_SUCCESS.equals(sourcePostingResponse.responseCode)) {
                        return destAccountRef.withAskTimeout(Duration.ofSeconds(5))
                                .ask(new AddPost(postingRequest.destinationAccount, postingRequest.reference, postingRequest.narrative,
                                        postingRequest.currencyCode, CreditDebitIndicator.Credit, postingRequest.amount));
                    } else {
                        //return the failure
                        return completedFuture(sourcePostingResponse);
                    }
                }).thenComposeAsync(finalPostingResponse -> {
                    log.info("Final Posting Resp : {}", finalPostingResponse);

                    if (Constants.RC_SUCCESS.equals(finalPostingResponse.responseCode)) {
                        log.info("Posting Successful");
                        return completedFuture(finalPostingResponse);
                    } else {
                        log.info("Posting was not successful, check indicator..");
                        if (CreditDebitIndicator.Credit.name().equals(finalPostingResponse.creditDebitIndicator)) {
                            log.info("Credit step failed, reverse the Debit..");
                            return sourceAccountRef.withAskTimeout(Duration.ofSeconds(5))
                                    .ask(new AddPost(postingRequest.sourceAccount, postingRequest.reference, "AUTO REVERSAL",
                                            postingRequest.currencyCode, CreditDebitIndicator.Credit, postingRequest.amount))
                                    .thenApplyAsync(autoReversalResponse -> {
                                        log.info("Auto Reversal Response : {}", autoReversalResponse);
                                        return finalPostingResponse;
                                    });
                        } else {
                            log.info("No further action needed");
                            return completedFuture(finalPostingResponse);
                        }
                    }
                }).thenApplyAsync(postingResponse -> ServiceResponse.of(postingResponse.responseCode, postingResponse.narrative))
                .exceptionally(throwable -> {
                    log.error("An error occurred performing postings : {}", throwable.getMessage());
                    throwable.printStackTrace();
                    //auto-reconciliation tool will flag and correct any potential residual issues
                    return ServiceResponse.of(Constants.RC_GENERAL_ERROR, "Posting error");
                });
    }

    @Override
    public Topic<com.globalbank.bookentry.api.AccountEvent> accountEvents() {
        // We want to publish all the shards of the account event
        return TopicProducer.taggedStreamWithOffset(AccountEvent.TAG.allTags(), (tag, offset) ->
                // Load the event stream for the passed in shard tag
                persistentEntityRegistry.eventStream(tag, offset).map(eventAndOffset -> {
                    // Now we want to convert from the persisted event to the published event.
                    // Although these two events are currently identical, in future they may
                    // change and need to evolve separately, by separating them now we save
                    // a lot of potential trouble in future.
                    com.globalbank.bookentry.api.AccountEvent eventToPublish;

                    if (eventAndOffset.first() instanceof AccountEvent.AccountDetailsChanged) {
                        AccountEvent.AccountDetailsChanged accountChanged = (AccountEvent.AccountDetailsChanged) eventAndOffset.first();
                        eventToPublish = new com.globalbank.bookentry.api.AccountEvent.AccountDetailsChanged(
                                accountChanged.accountNumber, accountChanged.accountName, accountChanged.accountType,
                                    accountChanged.timestamp);
                    } else if (eventAndOffset.first() instanceof AccountEvent.AccountAdded) {
                        AccountEvent.AccountAdded accountAdded = (AccountEvent.AccountAdded) eventAndOffset.first();
                        eventToPublish = new com.globalbank.bookentry.api.AccountEvent.AccountAdded(
                                accountAdded.accountNumber, accountAdded.accountName, accountAdded.accountType,
                                    accountAdded.currencyCode, accountAdded.timestamp);
                    } else if (eventAndOffset.first() instanceof AccountEvent.PostAdded) {
                            AccountEvent.PostAdded postAdded = (AccountEvent.PostAdded) eventAndOffset.first();
                            eventToPublish = new com.globalbank.bookentry.api.AccountEvent.PostAdded(
                                    postAdded.accountNumber, postAdded.reference, postAdded.narrative,
                                    postAdded.amount, postAdded.balance, postAdded.timestamp
                            );
                    } else {
                        throw new IllegalArgumentException("Unknown event: " + eventAndOffset.first());
                    }

                    // We return a pair of the translated event, and its offset, so that
                    // Lagom can track which offsets have been published.
                    return Pair.create(eventToPublish, eventAndOffset.second());
                })
        );
    }
}
