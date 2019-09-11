package com.globalbank.bookentry.impl;

import com.globalbank.bookentry.api.BookentryService;
import com.globalbank.bookentry.init.AccountInitializer;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

/**
 * The module that binds the BookentryService so that it can be served.
 */
public class BookentryModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(BookentryService.class, BookentryServiceImpl.class);

        bind(AccountInitializer.class).asEagerSingleton();
    }
}
