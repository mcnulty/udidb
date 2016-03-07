/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import java.nio.file.Paths;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.util.resource.Resource;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.inject.Guice;
import com.google.inject.Injector;

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

        String uiPath = System.getProperty("udidb.ui.path");
        boolean cors = Boolean.getBoolean("udidb.cors");

        server = new Server();
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(8888);
        server.addConnector(httpConnector);

        Injector injector = Guice.createInjector(new ServerModule());
        GuiceResteasyBootstrapServletContextListener resteasyListener = injector.getInstance(GuiceResteasyBootstrapServletContextListener.class);

        ServletHolder websocketEventsHolder = new ServletHolder(injector.getInstance(EventsServlet.class));

        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        contextHandler.setContextPath("/");
        contextHandler.addEventListener(resteasyListener);
        contextHandler.addServlet(websocketEventsHolder, "/events");
        contextHandler.addServlet(HttpServletDispatcher.class, "/*");

        if (cors) {
            FilterHolder crossOriginFilterHolder = contextHandler.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
            crossOriginFilterHolder.setInitParameter("allowedOrigins", "*");
        }

        ResourceHandler resourceHandler = new ResourceHandler();
        if (uiPath != null) {
            resourceHandler.setBaseResource(Resource.newResource(Paths.get(uiPath).toFile()));
        }else{
            resourceHandler.setBaseResource(Resource.newClassPathResource("/webui"));
        }

        ContextHandler resourceContextHandler = new ContextHandler();
        resourceContextHandler.setContextPath("/webui");
        resourceContextHandler.setHandler(resourceHandler);

        ContextHandlerCollection handlerCollection = new ContextHandlerCollection();
        handlerCollection.setHandlers(new Handler[]{ resourceContextHandler, contextHandler});

        server.setHandler(handlerCollection);

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

    public void stop() throws Exception
    {
        server.stop();
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
