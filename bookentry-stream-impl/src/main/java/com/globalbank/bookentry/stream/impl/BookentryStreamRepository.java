package com.globalbank.bookentry.stream.impl;

import akka.Done;
import com.datastax.driver.core.Row;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import com.globalbank.bookentry.stream.api.AccountStreamDto;
import com.globalbank.bookentry.stream.api.PostStreamDto;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class BookentryStreamRepository {

    private final CassandraSession uninitializedSession;

    // Will return the session when the Cassandra tables have been successfully created
    private volatile CompletableFuture<CassandraSession> initializedSession;

    @Inject
    public BookentryStreamRepository(CassandraSession uninitializedSession) {
        this.uninitializedSession = uninitializedSession;
        // Eagerly create the session
        session();
    }

    private static AccountStreamDto convertRowToAccountStreamDto(Row row) {
        return new AccountStreamDto(row.getString("account_number"), row.getString("account_name"),
                row.getString("account_type"), row.getString("currency_code"), row.getTimestamp("date_created").toInstant());
    }

    private static PostStreamDto convertRowToPostStreamDto(Row row) {
        return new PostStreamDto(row.getString("post_id"), row.getString("account_number"),
                row.getString("reference"), row.getString("narrative"), row.getDecimal("amount"),
                row.getDecimal("balance"), row.getTimestamp("date_created").toInstant());
    }

    private CompletionStage<CassandraSession> session() {
        // If there's no initialized session, or if the initialized session future completed
        // with an exception, then reinitialize the session and attempt to create the tables
        if (initializedSession == null || initializedSession.isCompletedExceptionally()) {
            return uninitializedSession.executeCreateTable(
                    "CREATE TABLE IF NOT EXISTS account (account_number text PRIMARY KEY, account_name text, account_type text, " +
                            "currency_code text, date_created timestamp)"
            ).thenComposeAsync(done1 -> {
                return uninitializedSession.executeCreateTable(
                        "CREATE TABLE IF NOT EXISTS account_post (post_id text PRIMARY KEY, account_number text, reference text, narrative text, " +
                                "amount decimal, balance decimal, date_created timestamp)"
                ).thenApplyAsync(done2 -> uninitializedSession).toCompletableFuture();
            });
        } else {
            return initializedSession;
        }
    }

    public CompletionStage<Done> createAccount(String accountNumber, String accountName, String accountType, String currencyCode, Instant timestamp) {
        return session().thenComposeAsync(session ->
                session.executeWrite("INSERT INTO account (account_number, account_name, account_type, currency_code, date_created) " +
                                "VALUES (?, ?, ?, ?, ?)",
                        accountNumber, accountName, accountType, currencyCode, Timestamp.from(timestamp))
        );
    }

    public CompletionStage<Done> createPost(String accountNumber, String reference, String narrative, BigDecimal amount, BigDecimal balance, Instant timestamp) {
        return session().thenComposeAsync(session ->
                session.executeWrite("INSERT INTO account_post (post_id, account_number, reference, narrative, amount, balance, date_created) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?)",
                        UUID.randomUUID().toString(), accountNumber, reference, narrative, amount, balance, Timestamp.from(timestamp))
        );
    }

    public CompletionStage<Optional<AccountStreamDto>> getAccount(String accountNumber) {
        return session().thenComposeAsync(session ->
                session.selectOne("SELECT account_number, account_name, account_type, currency_code, date_created " +
                        "FROM account WHERE account_number = ?", accountNumber)
        ).thenApplyAsync(maybeRow -> maybeRow.map(BookentryStreamRepository::convertRowToAccountStreamDto));
    }

    public CompletionStage<PSequence<AccountStreamDto>> getAllAccounts() {
        return session().thenComposeAsync(session ->
                session.selectAll("SELECT account_number, account_name, account_type, currency_code, date_created FROM account")
        ).thenApplyAsync(rows -> TreePVector.from(
                rows.stream()
                        .map(BookentryStreamRepository::convertRowToAccountStreamDto)
                        .collect(Collectors.toList())));
    }

    public CompletionStage<PSequence<PostStreamDto>> getAccountPosts(String accountNumber) {
        return session().thenComposeAsync(session ->
                session.selectAll("SELECT post_id, account_number, reference, narrative, amount, balance, date_created " +
                        "FROM account_post WHERE account_number = ? ALLOW FILTERING", accountNumber)
        ).thenApplyAsync(rows -> TreePVector.from(
                rows.stream()
                        .map(BookentryStreamRepository::convertRowToPostStreamDto)
                        .sorted(Comparator.comparing(postStreamDto -> postStreamDto.getDateCreated()))
                        .collect(Collectors.toList())));
    }

    public CompletionStage<PSequence<PostStreamDto>> getAllPosts() {
        return session().thenComposeAsync(session ->
                session.selectAll("SELECT post_id, account_number, reference, narrative, amount, balance, date_created FROM account_post")
        ).thenApplyAsync(rows -> TreePVector.from(
                rows.stream()
                        .map(BookentryStreamRepository::convertRowToPostStreamDto)
                        .collect(Collectors.toList())));
    }

}
