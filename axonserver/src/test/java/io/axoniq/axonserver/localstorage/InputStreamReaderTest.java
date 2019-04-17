/*
 * Copyright (c) 2017-2019 AxonIQ B.V. and/or licensed to AxonIQ B.V.
 * under one or more contributor license agreements.
 *
 *  Licensed under the AxonIQ Open Source License Agreement v1.0;
 *  you may not use this file except in compliance with the license.
 *
 */

package io.axoniq.axonserver.localstorage;

import io.axoniq.axonserver.util.AssertUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Marc Gathier
 */
public class InputStreamReaderTest {

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();
    private static TestInputStreamStorageContainer testStorageContainer;

    private EventStreamReader testSubject;

    @BeforeClass
    public static void init() throws Exception {
        testStorageContainer = new TestInputStreamStorageContainer(tempFolder.getRoot());
        testStorageContainer.createDummyEvents(1000, 100);
    }

    @AfterClass
    public static void close() {
        testStorageContainer.close();
    }


    @Before
    public void setUp() {
        testSubject = new EventStreamReader(testStorageContainer.getDatafileManagerChain(),
                                            testStorageContainer.getEventWriter()::registerEventListener, new EventStreamExecutor(1));
    }

    @Test
    public void readEventsFromStart() throws InterruptedException {
        AtomicLong counter = new AtomicLong();
        EventStreamController controller = testSubject.createController(eventWithToken -> {
            counter.incrementAndGet();
        }, Throwable::printStackTrace);

        controller.update(0, 100);
        AssertUtils.assertWithin(1000, TimeUnit.MILLISECONDS, () -> Assert.assertEquals(100, counter.get()));

        controller.update(0, 100);
        AssertUtils.assertWithin(1000, TimeUnit.MILLISECONDS, () -> Assert.assertEquals(200, counter.get()));
    }

    @Test
    public void readEventsFromEnd() throws InterruptedException {
        AtomicLong counter = new AtomicLong();
        EventStreamController controller = testSubject.createController(eventWithToken -> {
            counter.incrementAndGet();
        }, Throwable::printStackTrace);

        controller.update(testStorageContainer.getDatafileManagerChain().getLastToken() - 1, 100);
        AssertUtils.assertWithin(1000, TimeUnit.MILLISECONDS, () -> Assert.assertEquals(2, counter.get()));
    }

    @Test
    @Ignore
    public void readEventsWhileWriting() throws InterruptedException {
        AtomicLong counter = new AtomicLong();
        EventStreamController controller = testSubject.createController(eventWithToken -> counter.incrementAndGet(),
                                                                        Throwable::printStackTrace);

        controller.update(95000, 10000);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        Future<?> task = executor.submit(() -> {
            testStorageContainer.createDummyEvents(5000, 1, "live-");
        });

        AssertUtils.assertWithin(5000, TimeUnit.MILLISECONDS, () -> Assert.assertEquals(10000, counter.get()));
        if (!task.isDone()) task.cancel(true);
    }
}