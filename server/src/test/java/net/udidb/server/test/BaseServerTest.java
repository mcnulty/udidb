/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourcecrumbs.file.tests.NativeFileTestsInfo;
import net.udidb.server.api.models.UdiEventModel;
import net.udidb.server.driver.UdidbServer;
import net.udidb.server.test.events.JettyWampConnectorProvider;
import rx.Observable;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClient.State;
import ws.wamp.jawampa.WampClientBuilder;

/**
 * @author mcnulty
 */
public abstract class BaseServerTest
{
    private static final Logger logger = LoggerFactory.getLogger(BaseServerTest.class);

    private static final Path basePath = Paths.get(System.getProperty("native.file.tests.basePath"));
    private static final long EVENT_TIMEOUT_SECONDS = Long.getLong("udidb.server.tests.eventTimeout", 5);
    private static final UdidbServer udidbServer = new UdidbServer(new String[]{});

    private static NativeFileTestsInfo fileTestsInfo = null;

    private final Path binaryPath;
    private final String baseUri;
    private final String baseWsUri;
    private final LinkedBlockingQueue<UdiEventModel> eventQueue = new LinkedBlockingQueue<>();

    private WampClient eventsClient;

    protected BaseServerTest(String binaryPath)
    {
        this.binaryPath = fileTestsInfo.getFirstExecutablePath(binaryPath);
        this.baseUri = "http://localhost:8888";
        this.baseWsUri = "ws://localhost:8888";
    }

    public Path getBinaryPath()
    {
        return binaryPath;
    }

    public String getUri(String uri)
    {
        return baseUri + uri;
    }

    @BeforeClass
    public static void startServer() throws Exception
    {
        fileTestsInfo = new NativeFileTestsInfo(basePath);

        udidbServer.start();
    }

    @AfterClass
    public static void stopServer() throws Exception
    {
        udidbServer.stop();
    }

    @Before
    public void initializeEventsClient() throws Exception
    {
        eventQueue.clear();

        eventsClient = new WampClientBuilder()
                .withRealm("udidb")
                .withUri(baseWsUri + "/events")
                .withConnectorProvider(new JettyWampConnectorProvider()).build();

        CompletableFuture<Void> connectFuture = new CompletableFuture<>();
        eventsClient.statusChanged().subscribe((WampClient.State newState) -> {
            logger.debug("WampClient State => {}", newState);
            if (newState instanceof WampClient.ConnectedState) {
                eventsClient.makeSubscription("com.udidb.events", UdiEventModel.class)
                        .subscribe(
                                eventQueue::add,
                                (e) -> {
                                    throw new AssertionError(e);
                                });
                connectFuture.complete(null);
            }
        });
        eventsClient.open();

        try {
            // Wait for the connection to be initialized
            connectFuture.get(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }catch (TimeoutException e) {
            logger.error("Failed to initialize websocket", e);
            throw e;
        }
        logger.debug("Events WebSocket connected");
    }

    protected UdiEventModel waitForEvent() throws InterruptedException
    {
        return eventQueue.poll(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
