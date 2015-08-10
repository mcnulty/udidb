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
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import net.udidb.server.driver.UdidbServer;
import net.udidb.server.test.events.JettyWampConnectorProvider;
import rx.functions.Action1;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

/**
 * @author mcnulty
 */
public abstract class BaseServerTest
{
    private static final String basePath = System.getProperty("native.file.tests.basePath");

    private static final UdidbServer udidbServer = new UdidbServer(new String[]{});

    private final Path binaryPath;
    private final String baseUri;
    private final String baseWsUri;
    private final LinkedBlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();

    private WampClient eventsClient;

    protected BaseServerTest(String binaryPath)
    {
        this.binaryPath = Paths.get(basePath, binaryPath);
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
        eventsClient.statusChanged().subscribe((WampClient.State newState) -> {
            if (newState instanceof WampClient.ConnectedState) {
                eventsClient.makeSubscription("com.udidb.events", String.class)
                        .subscribe(
                                eventQueue::add,
                                (e) -> { throw new AssertionError(e); });
            }
        });
        eventsClient.open();
    }

    protected String waitForEvent() throws InterruptedException
    {
        return eventQueue.take();
    }
}
