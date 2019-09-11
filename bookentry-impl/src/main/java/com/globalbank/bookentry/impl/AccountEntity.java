package com.globalbank.bookentry.impl;

import akka.Done;
import com.globalbank.bookentry.api.ReadAccountDto;
import com.globalbank.bookentry.enums.AccountType;
import com.globalbank.bookentry.enums.CreditDebitIndicator;
import com.globalbank.bookentry.pojo.PostingResponse;
import com.globalbank.bookentry.util.Constants;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * This is an event sourced entity. It has a state, {@link AccountState}, which
 * stores what the account state should be.
 * <p>
 * Event sourced entities are interacted with by sending them commands. This
 * entity supports some commands, like a {@link AccountCommand.AddAccount} command, which is
 * used to add an account, and a {@link AccountCommand.UpdateAccount} command, which is used to update
 * account details.
 * <p>
 * Commands get translated to events, and it's the events that get persisted by
 * the entity. Each event will have an event handler registered for it, and an
 * event handler simply applies an event to the current state. This will be done
 * when the event is first created, and it will also be done when the entity is
 * loaded from the database - each event will be replayed to recreate the state
 * of the entity.
 * <p>
 * This entity defines some events, like the {@link AccountEvent.AccountAdded} event,
 * which is emitted when a {@link AccountCommand.AddAccount} command is received.
 */

@Slf4j
public class AccountEntity extends PersistentEntity<AccountCommand, AccountEvent, AccountState> {
    /**
     * An entity can define different behaviours for different states, but it will
     * always start with an initial behaviour.
     */
    @Override
    public Behavior initialBehavior(Optional<AccountState> snapshotState) {
        /*
         * Behaviour is defined using a behaviour builder. The behaviour builder
         * starts with a state, if this entity supports snapshotting (an
         * optimisation that allows the state itself to be persisted to combine many
         * events into one), then the passed in snapshotState may have a value that
         * can be used.
         *
         * Otherwise, the default state is to use the Hello greeting.
         */
        BehaviorBuilder b = newBehaviorBuilder(
                snapshotState.orElse(
                        new AccountState("init", "init", AccountType.System.name(), "NON",
                                BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), Instant.now().toString()))
        );

        /*
         * Command handler for the AddAccount command.
         */
        b.setCommandHandler(AccountCommand.AddAccount.class, (cmd, ctx) -> {
            // In response to this command, we want to persist it as a
            // AccountAdded event
            if (state().accountNumber.equals(cmd.accountNumber)) {
                log.info("Account already exists: {} with name {}", state().accountNumber, state().accountName);
                ctx.reply(Done.getInstance());
                return ctx.done();
            } else {
                log.info("Creating new account : {}|{}", cmd.accountNumber, cmd.accountName);
                return ctx.thenPersist(new AccountEvent.AccountAdded(cmd.accountNumber, cmd.accountName, cmd.accountType,
                                cmd.currencyCode, Instant.now().toString()),
                        // Then once the event is successfully persisted, we respond with done.
                        evt -> ctx.reply(Done.getInstance())
                );
            }
        });

        /*
         * Command handler for the UpdateAccount command.
         */
        b.setCommandHandler(AccountCommand.UpdateAccount.class, (cmd, ctx) ->
                // In response to this command, we want to persist it as a
                // AccountDetailsChanged event
                ctx.thenPersist(new AccountEvent.AccountDetailsChanged(cmd.accountNumber, cmd.accountName,
                                        cmd.accountType, state().currencyCode, Instant.now().toString()),
                        // Then once the event is successfully persisted, we respond with done.
                        evt -> ctx.reply(Done.getInstance())
                )
        );

        /*
         * Command handler for the GetOrInitSuspenseAccount command.
         */
        b.setCommandHandler(AccountCommand.GetOrInitSuspenseAccount.class, (cmd, ctx) -> {
            // In response to this command, we want to get an existing account or persist a new
            // AccountAdded event
            if (AccountType.Suspense.name().equals(state().accountType)) {
                log.info("Suspense account already initialized");
                ctx.reply(new ReadAccountDto(state().accountNumber, state().accountName, state().accountType,
                        state().currencyCode, state().balance, state().timestamp));
                return ctx.done();
            } else {
                return ctx.thenPersist(new AccountEvent.AccountAdded(cmd.accountNumber, cmd.accountName,
                                AccountType.Suspense.name(), cmd.currencyCode, Instant.now().toString()),
                        // Then once the event is successfully persisted, we respond with done.
                        evt -> ctx.reply(new ReadAccountDto(state().accountNumber, state().accountName, state().accountType,
                                state().currencyCode, state().balance, state().timestamp))
                );
            }
        });

        /*
         * Command handler for the AddPost command.
         */
        b.setCommandHandler(AccountCommand.AddPost.class, (cmd, ctx) -> {

            //check if debit entry (amount is NEGATIVE)
            if (CreditDebitIndicator.Debit.equals(cmd.creditDebitIndicator)) {
                log.info("This is a Debit Entry in Account type : {}", state().accountType);
                //reject non-Suspense accounts if insufficient funds
                if (AccountType.Suspense.name().equals(state().accountType)) {
                    log.info("Proceed to post");
                } else {
                    //check for overdraft
                    if (state().balance.compareTo(cmd.amount) == -1) {
                        log.info("Insufficient funds : Amount is {} but Balance is {}", cmd.amount, state().balance);
                        ctx.reply(new PostingResponse(Constants.RC_INSUFFICIENT_FUNDS, "Insufficient funds", cmd.creditDebitIndicator.name()));
                        return ctx.done();
                    } else {
                        log.info("Funds are available, proceed");
                    }
                }
            } else {
                log.info("This is a Credit Entry in Account type : {}", state().accountType);
            }

            // In response to this command, we want to persist it as a
            // PostAdded event
            BigDecimal amount = CreditDebitIndicator.Debit.equals(cmd.creditDebitIndicator) ?
                    cmd.amount.negate().setScale(2, BigDecimal.ROUND_HALF_UP) : cmd.amount.setScale(2, BigDecimal.ROUND_HALF_UP);

            return ctx.thenPersist(new AccountEvent.PostAdded(cmd.accountNumber, cmd.reference, cmd.narrative,
                            cmd.currencyCode, amount, state().balance.add(amount), Instant.now().toString()),
                    // Then once the event is successfully persisted, we respond with done.
                    evt -> ctx.reply(new PostingResponse(Constants.RC_SUCCESS, "Success", cmd.creditDebitIndicator.name()))
            );
        });

        /*
         * Command handler for the ReadAccount command.
         */
        b.setReadOnlyCommandHandler(AccountCommand.ReadAccount.class,
                // Get the account from the current state, and return the details
                (cmd, ctx) -> ctx.reply(new ReadAccountDto(state().accountNumber, state().accountName,
                        state().accountType, state().currencyCode, state().balance, state().timestamp))
        );

        /*
         * Event handler for the AccountAdded event.
         */
        b.setEventHandler(AccountEvent.AccountAdded.class,
                // We simply update the current state to reflect the new account details
                evt -> new AccountState(evt.accountNumber, evt.accountName, evt.accountType, evt.currencyCode,
                            BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP), Instant.now().toString())
        );


        /*
         * Event handler for the AccountDetailsChanged event.
         */
        b.setEventHandler(AccountEvent.AccountDetailsChanged.class,
                // We simply update the current state to reflect the new account details
                // Maintain the balance that is already there
                evt -> new AccountState(evt.accountNumber, evt.accountName, evt.accountType, evt.currencyCode,
                                state().balance, Instant.now().toString())
        );

        /*
         * Event handler for the PostAdded event.
         */
        b.setEventHandler(AccountEvent.PostAdded.class,
                // We simply update the current state to reflect the new account details
                evt -> {
                    //log.info("Handling PostAdded Event : {} {}", evt.accountNumber, evt.currencyCode + "" + evt.amount);

                    BigDecimal newBalance = state().balance.add(evt.amount);

                    //log.info("New ledger balance for account {} is {}", evt.accountNumber, newBalance);

                    return new AccountState(state().accountNumber, state().accountName, state().accountType,
                                    state().currencyCode, newBalance, Instant.now().toString());
                }
        );

        /*
         * We've defined all our behaviour, so build and return it.
         */
        return b.build();
    }
}
