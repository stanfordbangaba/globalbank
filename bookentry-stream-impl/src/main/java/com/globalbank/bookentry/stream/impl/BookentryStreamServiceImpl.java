package com.globalbank.bookentry.stream.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import com.globalbank.bookentry.api.BookentryService;
import com.globalbank.bookentry.stream.api.AccountStreamDto;
import com.globalbank.bookentry.stream.api.BookentryStreamService;
import com.globalbank.bookentry.stream.api.PostStreamDto;

import javax.inject.Inject;

import java.time.Instant;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class BookentryStreamServiceImpl implements BookentryStreamService {

    private final BookentryService bookentryService;
    private final BookentryStreamRepository repository;

    @Inject
    public BookentryStreamServiceImpl(BookentryService bookentryService, BookentryStreamRepository repository) {
        this.bookentryService = bookentryService;
        this.repository = repository;
    }

    @Override
    public ServiceCall<Source<String, NotUsed>, Source<AccountStreamDto, NotUsed>> directStream() {
        return accountNumbers -> completedFuture(
                accountNumbers.mapAsync(8, accountNumber ->
                        bookentryService.readAccount(accountNumber).invoke()
                                .thenApplyAsync(readAccountDto ->
                                        new AccountStreamDto(readAccountDto.accountNumber, readAccountDto.accountName,
                                                readAccountDto.accountType, readAccountDto.currencyCode,
                                                        Instant.parse(readAccountDto.timestamp)))));
    }

    @Override
    public ServiceCall<Source<String, NotUsed>, Source<AccountStreamDto, NotUsed>> autonomousStream() {
        return accountNumbers -> completedFuture(
                accountNumbers.mapAsync(8, accountNumber ->
                        repository.getAccount(accountNumber)
                                .thenApplyAsync(accountStreamDtoOptional -> accountStreamDtoOptional.get())
                                .thenApplyAsync(accountStreamDto ->
                                        new AccountStreamDto(accountStreamDto.getAccountNumber(), accountStreamDto.getAccountName(),
                                                accountStreamDto.getAccountType(), accountStreamDto.getCurrencyCode(),
                                                        accountStreamDto.getDateCreated()))));
    }

    @Override
    public ServiceCall<NotUsed, PSequence<AccountStreamDto>> getAccounts() {
        return notUsed -> repository.getAllAccounts()
                .thenApplyAsync(accountStreamDtos -> TreePVector.from(accountStreamDtos));
    }

    @Override
    public ServiceCall<NotUsed, PSequence<PostStreamDto>> getAccountPosts(String accountNumber) {
        return notUsed -> repository.getAccountPosts(accountNumber)
                .thenApplyAsync(postStreamDtos -> TreePVector.from(postStreamDtos));
    }
}
