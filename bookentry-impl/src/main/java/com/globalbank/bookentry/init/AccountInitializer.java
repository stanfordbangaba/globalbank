package com.globalbank.bookentry.init;

import akka.Done;
import com.globalbank.bookentry.enums.AccountType;
import com.globalbank.bookentry.impl.AccountCommand;
import com.globalbank.bookentry.util.Constants;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import lombok.extern.slf4j.Slf4j;
import com.globalbank.bookentry.impl.AccountEntity;

import javax.inject.Inject;
import java.util.concurrent.*;

@Slf4j
public class AccountInitializer {

    private final PersistentEntityRegistry persistentEntityRegistry;

    @Inject
    public AccountInitializer(PersistentEntityRegistry persistentEntityRegistry) {
        this.persistentEntityRegistry = persistentEntityRegistry;
        persistentEntityRegistry.register(AccountEntity.class);
    }

    public CompletionStage<PersistentEntityRef<AccountCommand>> getSuspenseAccountEntityRef(
            String accountNumber, String accountName, String currencyCode) {

        PersistentEntityRef<AccountCommand> sourceAccountRef = persistentEntityRegistry
                .refFor(AccountEntity.class, accountNumber);

        //get or initialize suspense account (avoids race conditions on init)
        return sourceAccountRef.ask(new AccountCommand.GetOrInitSuspenseAccount(accountNumber, accountName, currencyCode))
                .thenApplyAsync(doneCompletionStage -> sourceAccountRef);
    }
}
