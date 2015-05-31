/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import java.io.IOException;
import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Entry point for the udidb server
 *
 * @author mcnulty
 */
public final class UdidbServer
{
    private static final Logger logger = LoggerFactory.getLogger(UdidbServer.class);

    private static final String DEFAULT_BASE_URI = "http://localhost:8888/udidb/";
    private static final String API_RSRCS_PACKAGE = "net.udidb.server.api.resources";

    private final HttpServer httpServer;

    static
    {
        SLF4JBridgeHandler.removeHandlersForRootLogger();

        SLF4JBridgeHandler.install();
    }

    public UdidbServer(String[] args)
    {
        // TODO process args to configure the server

        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(DEFAULT_BASE_URI),
                new ResourceConfig().packages(API_RSRCS_PACKAGE));

        logger.debug("Started udidb server");
    }

    public void start() throws IOException
    {
        httpServer.start();
    }

    public void stop()
    {
        httpServer.shutdownNow();
    }

    public static void main(String[] args) throws InterruptedException, IOException
    {
        UdidbServer server = new UdidbServer(args);

        server.start();
        try {
            // Wait indefinitely
            Thread.sleep(Long.MAX_VALUE);
        }finally{
            server.stop();
        }
    }
}
