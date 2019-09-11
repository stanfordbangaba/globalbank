package com.globalbank.bookentry.api;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.broker.Topic;
import com.lightbend.lagom.javadsl.api.broker.kafka.KafkaProperties;
import com.lightbend.lagom.javadsl.api.transport.Method;

import static com.lightbend.lagom.javadsl.api.Service.*;

/**
 * The bookentry service interface.
 * The service descriptor also documents the API
 * <p>
 */
public interface BookentryService extends Service {

    ServiceCall<AddAccountDto, Done> addAccount();

    ServiceCall<UpdateAccountDto, Done> updateAccount();

    //readAccount returns latest account and balance information
    ServiceCall<NotUsed, ReadAccountDto> readAccount(String accountNumber);

    ServiceCall<DepositDto, ServiceResponse> performDeposit();

    ServiceCall<TransferDto, ServiceResponse> performTransfer();

    ServiceCall<ReversalDto, ServiceResponse> performReversal();

    /**
     * AccountEvents get published to Kafka.
     */
    Topic<AccountEvent> accountEvents();

    @Override
    default Descriptor descriptor() {
        return named("bookentry")
                .withCalls(
                        restCall(Method.POST, "/api/accounts", this::addAccount),
                        restCall(Method.PUT, "/api/accounts", this::updateAccount),
                        restCall(Method.GET, "/api/accounts/:accountNumber", this::readAccount),
                        restCall(Method.POST, "/api/accounts/deposit", this::performDeposit),
                        restCall(Method.POST, "/api/accounts/transfer", this::performTransfer),
                        restCall(Method.POST, "/api/accounts/reversal", this::performReversal)
                )
                .withTopics(
                        topic("account-events", this::accountEvents)
                                // Kafka partitions messages, messages within the same partition will
                                // be delivered in order, to ensure that all messages for the same user
                                // go to the same partition (and hence are delivered in order with respect
                                // to that user), we configure a partition key strategy that extracts the
                                // name as the partition key.
                                .withProperty(KafkaProperties.partitionKeyStrategy(), AccountEvent::getAccountNumber)
                )
                .withAutoAcl(true);
    }
}

