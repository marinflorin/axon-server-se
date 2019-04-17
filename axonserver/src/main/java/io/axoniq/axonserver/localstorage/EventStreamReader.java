/*
 * Copyright (c) 2017-2019 AxonIQ B.V. and/or licensed to AxonIQ B.V.
 * under one or more contributor license agreements.
 *
 *  Licensed under the AxonIQ Open Source License Agreement v1.0;
 *  you may not use this file except in compliance with the license.
 *
 */

package io.axoniq.axonserver.localstorage;

import io.axoniq.axonserver.grpc.event.EventWithToken;
import org.springframework.boot.actuate.health.Health;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Access to operations for tracking event processors. One instance per context and type (event/snapshot).
 * @author Marc Gathier
 */
public class EventStreamReader {
    private final EventStorageEngine eventStorageEngine;
    private final Function<Consumer<SerializedEventWithToken>, Registration> liveEventRegistrationFunction;
    private final EventStreamExecutor eventStreamExecutor;

    public EventStreamReader(EventStorageEngine datafileManagerChain,
                             Function<Consumer<SerializedEventWithToken>, Registration> liveEventRegistrationFunction,
                             EventStreamExecutor eventStreamExecutor) {
        this.eventStorageEngine = datafileManagerChain;
        this.liveEventRegistrationFunction = liveEventRegistrationFunction;
        this.eventStreamExecutor = eventStreamExecutor;
    }

    public EventStreamController createController(Consumer<SerializedEventWithToken> eventWithTokenConsumer,
                                                  Consumer<Throwable> errorCallback) {
        return new EventStreamController(eventWithTokenConsumer, errorCallback, eventStorageEngine,
                                         liveEventRegistrationFunction, eventStreamExecutor);
    }

    public Iterator<SerializedTransactionWithToken> transactionIterator(long firstToken, long limitToken) {
        return eventStorageEngine.transactionIterator(firstToken, limitToken);
    }

    public void query(long minToken, long minTimestamp, Predicate<EventWithToken> consumer) {
        eventStorageEngine.query(minToken, minTimestamp, consumer);
    }

    /**
     * Returns the first token in the event store for the current context. Returns -1 if event store is empty.
     * @return the first token in this event store
     */
    public long getFirstToken() {
        return eventStorageEngine.getFirstToken();
    }

    /**
     * Returns the token for the first event at or after the specified instant.
     * @param instant timestamp to check
     * @return the token
     */
    public long getTokenAt(long instant) {
        return eventStorageEngine.getTokenAt(instant);
    }

    public void health(Health.Builder builder) {
        eventStorageEngine.health(builder);
    }

    /**
     * Returns the last token in the event store.
     * @return the last token
     */
    public long getLastToken() {
        return eventStorageEngine.getLastToken();
    }
}