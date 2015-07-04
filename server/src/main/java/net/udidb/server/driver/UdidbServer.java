/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
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

    private final Server server;

    public UdidbServer(String[] args)
    {
        // TODO process args to configure the server

        server = new Server();
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(8888);
        server.addConnector(httpConnector);

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.setInitParameter("resteasy.guice.modules", ServerModule.class.getCanonicalName());
        contextHandler.addEventListener(new GuiceResteasyBootstrapServletContextListener());
        contextHandler.addServlet(HttpServletDispatcher.class, "/*");

        server.setHandler(contextHandler);

        logger.debug("Started udidb server");
    }

    public void start() throws Exception
    {
        server.start();
    }

    public void join() throws Exception
    {
        server.join();
    }

    private static void initializeLogging() throws Exception
    {
        Log.setLog(new Slf4jLog());

        SLF4JBridgeHandler.removeHandlersForRootLogger();

        SLF4JBridgeHandler.install();
    }

    public static void main(String[] args) throws Exception
    {
        initializeLogging();

        UdidbServer server = new UdidbServer(args);

        server.start();
        server.join();
    }
}
