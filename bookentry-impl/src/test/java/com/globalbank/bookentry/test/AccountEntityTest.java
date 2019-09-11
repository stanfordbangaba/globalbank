package com.globalbank.bookentry.test;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.globalbank.bookentry.enums.CreditDebitIndicator;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;

import com.globalbank.bookentry.api.ReadAccountDto;
import com.globalbank.bookentry.api.ServiceResponse;
import com.globalbank.bookentry.enums.AccountType;
import com.globalbank.bookentry.impl.AccountCommand;
import com.globalbank.bookentry.impl.AccountCommand.AddAccount;
import com.globalbank.bookentry.impl.AccountCommand.ReadAccount;
import com.globalbank.bookentry.impl.AccountEntity;
import com.globalbank.bookentry.impl.AccountEvent;
import com.globalbank.bookentry.impl.AccountState;

import static org.junit.Assert.*;

@Slf4j
public class AccountEntityTest {
    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("AccountEntityTest");
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAccountEntity() {
        PersistentEntityTestDriver<AccountCommand, AccountEvent, AccountState> driver = new PersistentEntityTestDriver<>(system,
                new AccountEntity(), "123");

        /**
         *          Test Add Account Scenario (should create and read the created account)
         */

        log.info("OUTCOME 1 : Should ADD an Account and emit an AccountAdded event");

        final Outcome<AccountEvent, AccountState> outcome1 = driver.run(
                new AddAccount("123", "Stan Bangaba", AccountType.Savings.name(), "GBP"));

        System.out.println("Add Account: Replies -> " + outcome1.getReplies().get(0));
        System.out.println("Add Account: State -> " + outcome1.state());
        System.out.println("Add Account: Events -> " + outcome1.events());

        assertEquals(1, outcome1.getReplies().size());
        assertEquals(Done.getInstance(), outcome1.getReplies().get(0));
        //assertEquals(Collections.emptyList(), outcome1.issues());

        assertEquals("123", outcome1.state().accountNumber);
        assertEquals("Stan Bangaba", outcome1.state().accountName);
        assertEquals(AccountType.Savings.name(), outcome1.state().accountType);
        assertEquals("GBP", outcome1.state().currencyCode);
        assertEquals(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), outcome1.state().balance);
        assertNotNull(outcome1.state().timestamp);

        assertEquals(1, outcome1.events().size());

        final AccountEvent.AccountAdded event1 = (AccountEvent.AccountAdded) outcome1.events().get(0);

        assertEquals("123", event1.accountNumber);
        assertEquals("Stan Bangaba", event1.accountName);
        assertEquals(AccountType.Savings.name(), event1.accountType);
        assertEquals("GBP", event1.currencyCode);
        assertNotNull(event1.timestamp);

        /**
         *          Test Read Account Scenario (should read account details)
         */

        log.info("OUTCOME 2 : Should READ the Account and emit no events");

        final Outcome<AccountEvent, AccountState> outcome2 = driver.run(new ReadAccount("123"));

        System.out.println("Read Account: Replies -> " + outcome2.getReplies().get(0));
        System.out.println("Read Account: State -> " + outcome2.state());
        System.out.println("Read Account: Events -> " + outcome2.events());

        assertEquals(1, outcome2.getReplies().size());
        //assertEquals(Collections.emptyList(), outcome2.issues());

        final ReadAccountDto readAccountDto = (ReadAccountDto) outcome2.getReplies().get(0);

        assertEquals("123", readAccountDto.accountNumber);
        assertEquals("Stan Bangaba", readAccountDto.accountName);
        assertEquals(AccountType.Savings.name(), readAccountDto.accountType);
        assertEquals("GBP", readAccountDto.currencyCode);
        assertEquals(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), readAccountDto.balance);

        assertEquals(0, outcome2.events().size());

        /**
         *          Test Update Account Scenario (should update account details)
         */

        log.info("OUTCOME 3 : Should UPDATE account details");

        Outcome<AccountEvent, AccountState> outcome3 = driver.run(
                new AccountCommand.UpdateAccount("123", "Stanford Bangaba", AccountType.Savings.name()));

        System.out.println("Update Account: Replies -> " + outcome3.getReplies().get(0));
        System.out.println("Update Account: State -> " + outcome3.state());
        System.out.println("Update Account: Events -> " + outcome3.events());

        assertEquals(1, outcome3.getReplies().size());
        assertEquals(Done.getInstance(), outcome3.getReplies().get(0));
        //assertEquals(Collections.emptyList(), outcome3.issues());

        assertEquals("123", outcome3.state().accountNumber);
        assertEquals("Stanford Bangaba", outcome3.state().accountName);
        assertEquals(AccountType.Savings.name(), outcome3.state().accountType);
        assertEquals("GBP", outcome3.state().currencyCode);
        assertEquals(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), outcome3.state().balance);
        assertNotNull(outcome3.state().timestamp);

        assertEquals(1, outcome3.events().size());

        final AccountEvent.AccountDetailsChanged event3 = (AccountEvent.AccountDetailsChanged) outcome3.events().get(0);

        assertEquals("123", event3.accountNumber);
        assertEquals("Stanford Bangaba", event3.accountName);
        assertEquals(AccountType.Savings.name(), event3.accountType);
        assertEquals("GBP", event3.currencyCode);
        assertNotNull(event3.timestamp);

        /**
         *          Test Add Post Scenario (should add an account post)
         */

        log.info("OUTCOME 4 : Should Add a CREDIT POST");

        Outcome<AccountEvent, AccountState> outcome4 = driver.run(
                new AccountCommand.AddPost("123", "ref1", "Deposit", "GBP",
                        CreditDebitIndicator.Credit, BigDecimal.valueOf(10.00)));

        System.out.println("Add Post: Replies -> " + outcome4.getReplies().get(0));
        System.out.println("Add Post: State -> " + outcome4.state());
        System.out.println("Add Post: Events -> " + outcome4.events());

        assertEquals(1, outcome4.getReplies().size());
        //assertEquals(Collections.emptyList(), outcome4.issues());

        assertEquals(BigDecimal.TEN.setScale(2, BigDecimal.ROUND_HALF_UP), outcome4.state().balance);

        assertEquals(1, outcome4.events().size());

        final AccountEvent.PostAdded event4 = (AccountEvent.PostAdded) outcome4.events().get(0);

        assertEquals("123", event4.accountNumber);
        assertEquals("ref1", event4.reference);
        assertEquals("GBP", event4.currencyCode);
        assertEquals(BigDecimal.TEN.setScale(2, BigDecimal.ROUND_HALF_UP), event4.amount);
        assertEquals(BigDecimal.TEN.setScale(2, BigDecimal.ROUND_HALF_UP), event4.balance);
        assertNotNull("Deposit", event4.narrative);
        assertNotNull(event4.timestamp);

        log.info("OUTCOME 5 : Should Add Another CREDIT POST");

        Outcome<AccountEvent, AccountState> outcome5 = driver.run(
                new AccountCommand.AddPost("123", "ref2", "Transfer", "GBP",
                        CreditDebitIndicator.Credit, BigDecimal.valueOf(50.00)));

        System.out.println("Add Post: Replies -> " + outcome5.getReplies().get(0));
        System.out.println("Add Post: State -> " + outcome5.state());
        System.out.println("Add Post: Events -> " + outcome5.events());

        assertEquals(1, outcome5.getReplies().size());
        //assertEquals(Collections.emptyList(), outcome5.issues());

        assertEquals(new BigDecimal(60.00).setScale(2, BigDecimal.ROUND_HALF_UP), outcome5.state().balance);

        assertEquals(1, outcome5.events().size());

        final AccountEvent.PostAdded event5 = (AccountEvent.PostAdded) outcome5.events().get(0);

        assertEquals("123", event5.accountNumber);
        assertEquals("ref2", event5.reference);
        assertEquals("GBP", event5.currencyCode);
        assertEquals(new BigDecimal(50.00).setScale(2, BigDecimal.ROUND_HALF_UP), event5.amount);
        assertEquals(new BigDecimal(60.00).setScale(2, BigDecimal.ROUND_HALF_UP), event5.balance);
        assertNotNull("Deposit", event5.narrative);
        assertNotNull(event5.timestamp);

        log.info("OUTCOME 6 : Should Add a DEBIT POST");

        Outcome<AccountEvent, AccountState> outcome6 = driver.run(
                new AccountCommand.AddPost("123", "ref3", "Transfer", "GBP",
                        CreditDebitIndicator.Debit, BigDecimal.valueOf(10.00)));

        System.out.println("Add Post: Replies -> " + outcome6.getReplies().get(0));
        System.out.println("Add Post: State -> " + outcome6.state());
        System.out.println("Add Post: Events -> " + outcome6.events());

        assertEquals(1, outcome6.getReplies().size());
        //assertEquals(Collections.emptyList(), outcome6.issues());

        assertEquals(new BigDecimal(50.00).setScale(2, BigDecimal.ROUND_HALF_UP), outcome6.state().balance);

        assertEquals(1, outcome5.events().size());

        final AccountEvent.PostAdded event6 = (AccountEvent.PostAdded) outcome6.events().get(0);

        assertEquals("123", event6.accountNumber);
        assertEquals("ref3", event6.reference);
        assertEquals("GBP", event6.currencyCode);
        assertEquals(BigDecimal.TEN.setScale(2, BigDecimal.ROUND_HALF_UP).negate(), event6.amount);
        assertEquals(new BigDecimal(50.00).setScale(2, BigDecimal.ROUND_HALF_UP), event6.balance);
        assertNotNull("Transfer", event6.narrative);
        assertNotNull(event6.timestamp);

        log.info("OUTCOME 7 : Should Add Another DEBIT POST");

        Outcome<AccountEvent, AccountState> outcome7 = driver.run(
                new AccountCommand.AddPost("123", "ref3", "Transfer", "GBP",
                        CreditDebitIndicator.Debit, BigDecimal.valueOf(50.00)));

        System.out.println("Add Post: Replies -> " + outcome7.getReplies().get(0));
        System.out.println("Add Post: State -> " + outcome7.state());
        System.out.println("Add Post: Events -> " + outcome7.events());

        assertEquals(1, outcome7.getReplies().size());
        //assertEquals(Collections.emptyList(), outcome7.issues());

        assertEquals(new BigDecimal(0.00).setScale(2, BigDecimal.ROUND_HALF_UP), outcome7.state().balance);

        assertEquals(1, outcome7.events().size());

        final AccountEvent.PostAdded event7 = (AccountEvent.PostAdded) outcome7.events().get(0);

        assertEquals("123", event7.accountNumber);
        assertEquals("ref3", event7.reference);
        assertEquals("GBP", event7.currencyCode);
        assertEquals(new BigDecimal(50.00).setScale(2, BigDecimal.ROUND_HALF_UP).negate(), event7.amount);
        assertEquals(BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), event7.balance);
        assertNotNull("Transfer", event7.narrative);
        assertNotNull(event7.timestamp);

        log.info("End AccountEntity Tests");
    }
}
