package com.globalbank.bookentry.stream.impl;

import akka.Done;
import akka.stream.javadsl.Flow;
import lombok.extern.slf4j.Slf4j;
import com.globalbank.bookentry.api.AccountEvent;
import com.globalbank.bookentry.api.BookentryService;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * This subscribes to the BookentryService event stream.
 */

@Slf4j
public class BookentryStreamSubscriber {

    @Inject
    public BookentryStreamSubscriber(BookentryService bookentryService, BookentryStreamRepository repository) {
        // Create a subscriber
        bookentryService.accountEvents().subscribe()
                // And subscribe to it with at least once processing semantics.
                .atLeastOnce(
                        // Create a flow that emits a Done for each message it processes
                        Flow.<AccountEvent>create().mapAsync(1, event -> {
                            log.info("Got Event  : {} | {}", event.getAccountNumber(), event.getClass().getSimpleName());
                            if (event instanceof AccountEvent.AccountAdded) {
                                AccountEvent.AccountAdded accountAdded = (AccountEvent.AccountAdded) event;
                                log.info("Received AccountAdded Event : {}|{}", accountAdded.accountNumber, accountAdded.accountName);
                                // Create the account
                                return repository.createAccount(accountAdded.getAccountNumber(), accountAdded.getAccountName(),
                                        accountAdded.accountType, accountAdded.currencyCode, Instant.parse(accountAdded.timestamp));
                            } else if (event instanceof AccountEvent.PostAdded) {
                                AccountEvent.PostAdded postAdded = (AccountEvent.PostAdded) event;
                                log.info("Received PostAdded Event : {}|{}", postAdded.accountNumber, postAdded.amount);
                                // Create the post
                                return repository.createPost(postAdded.getAccountNumber(), postAdded.getReference(),
                                        postAdded.getNarrative(), postAdded.getAmount(), postAdded.getBalance(),
                                            Instant.parse(postAdded.timestamp));
                            } else {
                                // Ignore all other events
                                return CompletableFuture.completedFuture(Done.getInstance());
                            }
                        })
                );
    }
}
