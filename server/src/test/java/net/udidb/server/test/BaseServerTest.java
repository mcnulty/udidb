/*
 * Copyright (c) 2011-2017, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.ValidatableResponse;

import net.libudi.api.event.EventType;
import net.libudi.nativefiletests.NativeFileTestsInfo;
import net.udidb.server.api.models.DebuggeeConfigModel;
import net.udidb.server.api.models.DebuggeeContextModel;
import net.udidb.server.api.models.OperationModel;
import net.udidb.server.api.models.ProcessModel;
import net.udidb.server.api.models.UdiEventModel;
import net.udidb.server.test.events.JettyWampConnectorProvider;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Base class for a server test
 */
public abstract class BaseServerTest
{
    private static final Logger logger = LoggerFactory.getLogger(BaseServerTest.class);

    private static final Path basePath = Paths.get(System.getProperty("native.file.tests.basePath"));
    private static final long EVENT_TIMEOUT_SECONDS = Long.getLong("udidb.server.tests.eventTimeout", 5);

    private static NativeFileTestsInfo fileTestsInfo = null;

    static {
        if (fileTestsInfo == null) {
            try {
                fileTestsInfo = new NativeFileTestsInfo(basePath);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
    }

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

    protected DebuggeeContextModel createDebuggee()
    {
        DebuggeeConfigModel configModel = new DebuggeeConfigModel();
        configModel.setExecPath(getBinaryPath().toString());

        DebuggeeContextModel contextModel = given()
                .contentType("application/json")
                .body(configModel)
                .when()
                .post(getUri("/debuggeeContexts"))
                .as(DebuggeeContextModel.class);

        assertFalse(StringUtils.isEmpty(contextModel.getId()));
        assertEquals(configModel.getExecPath(), contextModel.getExecPath());

        return contextModel;
    }

    protected ValidatableResponse continueContext(DebuggeeContextModel contextModel)
    {
        OperationModel continueOp = new OperationModel();
        continueOp.setName("continue");

        return submitOperation(contextModel, continueOp).body("name", equalTo("continue"));
    }

    protected void waitForExit(DebuggeeContextModel contextModel,
                               int exitCode) throws InterruptedException
    {
        // Wait for the exit event
        UdiEventModel event = waitForEvent();
        assertNotNull(event);
        assertEquals(contextModel.getId(), event.getContextId());
        assertEquals(EventType.PROCESS_EXIT, event.getEventType());
        assertEquals(event.getEventData(), ImmutableMap.of("exitCode", exitCode));
    }

    protected ValidatableResponse submitOperation(DebuggeeContextModel contextModel, OperationModel operationModel)
    {
        return given()
                .contentType("application/json")
                .body(operationModel)
                .when()
                .post(getUri(String.format("/debuggeeContexts/%s/process/operation", contextModel.getId())))
                .then();
    }
}
