package com.globalbank.bookentry.stream.impl;

import com.globalbank.bookentry.api.BookentryService;
import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.globalbank.bookentry.stream.api.BookentryStreamService;

/**
 * The module that binds the BookentryStreamService so that it can be served.
 */
public class BookentryStreamModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        // Bind the BookentryStreamService service
        bindService(BookentryStreamService.class, BookentryStreamServiceImpl.class);
        // Bind the BookentryService client
        bindClient(BookentryService.class);
        // Bind the subscriber eagerly to ensure it starts up
        bind(BookentryStreamSubscriber.class).asEagerSingleton();
    }
}
