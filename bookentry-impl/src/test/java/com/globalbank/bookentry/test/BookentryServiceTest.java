package com.globalbank.bookentry.test;

import akka.Done;
import com.globalbank.bookentry.api.*;
import org.junit.Test;

import com.globalbank.bookentry.enums.AccountType;

import java.math.BigDecimal;
import java.util.concurrent.CompletionStage;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.withServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

public class BookentryServiceTest {

    @Test
    public void shouldAddAccount() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final Done done = service.addAccount().invoke(
                    new AddAccountDto("1", "Ethan Bangaba", AccountType.Current.name(), "GBP")
            ).toCompletableFuture().get(5, SECONDS);

            assertNotNull(done);
        });
    }

    @Test
    public void shouldReadAccount() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final ReadAccountDto readAccountDto = service.addAccount().invoke(
                    new AddAccountDto("2", "Sharon Bangaba", AccountType.Current.name(), "USD")
            ).thenComposeAsync(done -> {
                return service.readAccount("2").invoke();
            }).toCompletableFuture().get(5, SECONDS);

            assertEquals("2", readAccountDto.accountNumber);
            assertEquals("Sharon Bangaba", readAccountDto.accountName);
            assertEquals(AccountType.Current.name(), readAccountDto.accountType);
            assertEquals("USD", readAccountDto.currencyCode);
            assertEquals(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), readAccountDto.balance);
        });
    }

    @Test
    public void shouldUpdateAccountDetails() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final ReadAccountDto readAccountDto = service.addAccount().invoke(
                    new AddAccountDto("3", "Plaxcedes Bangaba", AccountType.Savings.name(), "ZWL")
            ).thenComposeAsync(done -> {
                return service.updateAccount().invoke(
                        new UpdateAccountDto("3", "Bester Bangaba", AccountType.Current.name()));
            }).thenComposeAsync(done -> {
                return service.readAccount("3").invoke();
            }).toCompletableFuture().get(5, SECONDS);

            assertEquals("3", readAccountDto.accountNumber);
            assertEquals("Bester Bangaba", readAccountDto.accountName);
            assertEquals(AccountType.Current.name(), readAccountDto.accountType);
            assertEquals("ZWL", readAccountDto.currencyCode);
            assertEquals(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), readAccountDto.balance);
        });
    }

    @Test
    public void shouldDeposit() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final CompletionStage<Done> account1Future = service.addAccount().invoke(
                    new AddAccountDto("4", "Plaxcedes Bangaba", AccountType.Savings.name(), "ZWL"));

            final ServiceResponse serviceResponse = account1Future
                    .thenComposeAsync(done -> service.performDeposit().invoke(
                            new DepositDto("DEP01", "4", "ZWL", new BigDecimal(20))))
                    .toCompletableFuture().get(5, SECONDS);

            assertEquals("00", serviceResponse.responseCode);
            assertTrue(serviceResponse.narrative.contains("Success"));
        });
    }

    @Test
    public void shouldRejectDepositWithInvalidAmount() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final CompletionStage<Done> account1Future = service.addAccount().invoke(
                    new AddAccountDto("41", "Plaxcedes Bangaba", AccountType.Savings.name(), "ZWL"));

            final ServiceResponse serviceResponse = account1Future
                    .thenComposeAsync(done -> service.performDeposit().invoke(
                            new DepositDto("DEP01F", "41", "ZWL", new BigDecimal(-30))))
                    .toCompletableFuture().get(5, SECONDS);

            assertEquals("13", serviceResponse.responseCode);
            assertTrue(serviceResponse.narrative.contains("Invalid"));
        });
    }

    @Test
    public void shouldTransfer() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final CompletionStage<Done> account1Future = service.addAccount().invoke(
                    new AddAccountDto("5", "Plaxcedes Bangaba", AccountType.Savings.name(), "ZWL"));

            final CompletionStage<Done> account2Future = service.addAccount().invoke(
                    new AddAccountDto("6", "Devine Bangaba", AccountType.Savings.name(), "ZWL"));

            final ServiceResponse serviceResponse = account1Future.thenCombineAsync(account2Future, (done, done2) -> {
                return service.performDeposit().invoke(new DepositDto("DEP02", "5", "ZWL", new BigDecimal(20)));
            }).thenComposeAsync(serviceResponseCompletionStage -> serviceResponseCompletionStage)
                    .thenComposeAsync(serviceResponse1 -> {
                        assertEquals("00", serviceResponse1.responseCode);
                        return service.performTransfer().invoke(
                                new TransferDto("TRF01", "5", "6", "ZWL", new BigDecimal(10)));
                    }).toCompletableFuture().get(5, SECONDS);

            assertEquals("00", serviceResponse.responseCode);
            assertTrue(serviceResponse.narrative.contains("Success"));
        });
    }

    @Test
    public void shouldRejectTransferWithInvalidAmount() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final CompletionStage<Done> account1Future = service.addAccount().invoke(
                    new AddAccountDto("51", "Plaxcedes Bangaba", AccountType.Savings.name(), "GBP"));

            final CompletionStage<Done> account2Future = service.addAccount().invoke(
                    new AddAccountDto("61", "Devine Bangaba", AccountType.Savings.name(), "GBP"));

            final ServiceResponse serviceResponse = service.performTransfer().invoke(
                                new TransferDto("TRF02", "7", "8", "GBP", new BigDecimal(-50))
                    ).toCompletableFuture().get(5, SECONDS);

            assertEquals("13", serviceResponse.responseCode);
            assertTrue(serviceResponse.narrative.contains("Invalid amount"));
        });

    }

    @Test
    public void shouldRejectTransferWithInsufficientFunds() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final CompletionStage<Done> account1Future = service.addAccount().invoke(
                    new AddAccountDto("7", "Plaxcedes Bangaba", AccountType.Savings.name(), "GBP"));

            final CompletionStage<Done> account2Future = service.addAccount().invoke(
                    new AddAccountDto("8", "Devine Bangaba", AccountType.Savings.name(), "GBP"));

            final ServiceResponse serviceResponse = account1Future.thenCombineAsync(account2Future, (done, done2) -> {
                return service.performDeposit().invoke(new DepositDto("DEP03", "7", "GBP", new BigDecimal(10)));
            }).thenComposeAsync(serviceResponseCompletionStage -> serviceResponseCompletionStage)
                    .thenComposeAsync(serviceResponse1 -> {
                        assertEquals("00", serviceResponse1.responseCode);
                        return service.performTransfer().invoke(
                                new TransferDto("TRF02", "7", "8", "GBP", new BigDecimal(50)));
                    }).toCompletableFuture().get(5, SECONDS);

            assertEquals("81", serviceResponse.responseCode);
            assertTrue(serviceResponse.narrative.contains("Insufficient funds"));
        });

    }

    @Test
    public void shouldReverse() {
        withServer(defaultSetup().withCassandra(), server -> {
            BookentryService service = server.client(BookentryService.class);

            final CompletionStage<Done> account1Future = service.addAccount().invoke(
                    new AddAccountDto("9", "Plaxcedes Bangaba", AccountType.Savings.name(), "ZAR"));

            final CompletionStage<Done> account2Future = service.addAccount().invoke(
                    new AddAccountDto("10", "Stan Bangaba", AccountType.Savings.name(), "ZAR"));

            final ServiceResponse serviceResponse = account1Future.thenCombineAsync(account2Future, (done, done2) -> {
                return service.performDeposit().invoke(new DepositDto("DEP05", "9", "ZAR", new BigDecimal(20)));
            }).thenComposeAsync(serviceResponseCompletionStage -> serviceResponseCompletionStage)
                    .thenComposeAsync(serviceResponse1 -> {
                        assertEquals("00", serviceResponse1.responseCode);
                        return service.performTransfer().invoke(
                                new TransferDto("TRF05", "9", "10", "ZAR", new BigDecimal(10)));
                    }).thenComposeAsync(serviceResponse1 -> {
                        return service.performReversal().invoke(
                                new ReversalDto("TRF05", "9", "10", "ZAR", new BigDecimal(10)));
                    })
                    .toCompletableFuture().get(5, SECONDS);

            assertEquals("00", serviceResponse.responseCode);
            assertTrue(serviceResponse.narrative.contains("Success"));
        });
    }
}
