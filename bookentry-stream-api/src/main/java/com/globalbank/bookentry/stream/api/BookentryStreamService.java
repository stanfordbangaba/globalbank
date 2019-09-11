package com.globalbank.bookentry.stream.api;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import org.pcollections.PSequence;

import static com.lightbend.lagom.javadsl.api.Service.*;
import static com.lightbend.lagom.javadsl.api.Service.namedCall;

public interface BookentryStreamService extends Service {
    /**
     * This stream is implemented by asking the account service directly to return
     * account details to each passed in account number. It requires the account service to be up
     * and running to function.
     */
    ServiceCall<Source<String, NotUsed>, Source<AccountStreamDto, NotUsed>> directStream();

    /**
     * This stream is implemented autonomously, it uses its own store, populated
     * by subscribing to the events published by the account service, to return account
     * details to each passed in account number. It can function even when the account service is
     * down. Eventually consistent.
     */
    ServiceCall<Source<String, NotUsed>, Source<AccountStreamDto, NotUsed>> autonomousStream();

    /**
     * Get all accounts. Eventually consistent. Mainly for reporting and analysis.
     */
    ServiceCall<NotUsed, PSequence<AccountStreamDto>> getAccounts();

    /**
     * Get all account postings. Eventually consistent. Mainly for reporting and analysis.
     */
    ServiceCall<NotUsed, PSequence<PostStreamDto>> getAccountPosts(String accountNumber);

    @Override
    default Descriptor descriptor() {
        return named("account-stream")
                .withCalls(
                        namedCall("/stream/accounts/direct-stream", this::directStream),
                        namedCall("/stream/accounts/auto-stream", this::autonomousStream),
                        restCall(Method.GET,"/stream/accounts", this::getAccounts),
                        restCall(Method.GET, "/stream/accounts/:accountNumber/posts", this::getAccountPosts)
                )
                .withAutoAcl(true);
    }
}
