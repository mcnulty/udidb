/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.englishtown.vertx.guice.GuiceJerseyServer;
import com.englishtown.vertx.jersey.JerseyHandler;
import com.englishtown.vertx.jersey.VertxContainer;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

/**
 * Entry point for the udidb server
 *
 * @author mcnulty
 */
public final class UdidbServer
{
    private static final Logger logger = LoggerFactory.getLogger(UdidbServer.class);

    private static AtomicBoolean loggingInitialized = new AtomicBoolean(false);

    private final HttpServer httpServer;
    private final VertxContainer vertxContainer;
    private final JerseyHandler jerseyHandler;

    public UdidbServer(String[] args)
    {
        // TODO process args to configure the server

        initializeLogging();

        String uiPath = System.getProperty("udidb.ui.path");
        boolean cors = Boolean.getBoolean("udidb.cors");

        Injector injector = Guice.createInjector(new ServerModule());

        // This will initialize the Jersey HK2 bridge
        injector.getInstance(GuiceJerseyServer.class);

        Vertx vertx = injector.getInstance(Vertx.class);
        this.httpServer = vertx.createHttpServer(new HttpServerOptions().setWebsocketSubProtocols("wamp.2.json"));

        Router router = Router.router(vertx);

        if (cors) {
            router.route().handler(CorsHandler.create("*")
                                              .allowedHeader("Content-Type"));
        }

        // static content for the UI
        StaticHandler staticHandler = StaticHandler.create();
        if (uiPath != null) {
            staticHandler.setAllowRootFileSystemAccess(true);
            staticHandler.setWebRoot(uiPath);
        }else{
            staticHandler.setWebRoot("webui");
        }
        router.route("/webui/*").handler(staticHandler);

        // WebSocket events
        this.httpServer.websocketHandler(websocket -> {
            if (!websocket.path().equals("/events")) {
                websocket.reject();
            }

            injector.getInstance(EventsSocket.class).setServerWebSocket(websocket);
        });

        // API resources
        this.vertxContainer = injector.getInstance(VertxContainer.class);
        this.jerseyHandler = injector.getInstance(JerseyHandler.class);

        router.routeWithRegex("/.*").handler(context -> jerseyHandler.handle(context.request()));

        httpServer.requestHandler(router::accept);
    }

    public void start() throws Exception
    {
        vertxContainer.start();

        CompletableFuture<Void> listenComplete = new CompletableFuture<>();
        httpServer.listen(8888, result -> {
            if (result.succeeded()) {
                listenComplete.complete(null);
            }else{
                logger.error("Failed to start server", result.cause());
                listenComplete.completeExceptionally(result.cause());
            }
        });
        listenComplete.get();
        logger.info("Listening on port 8888");
    }

    public void join() throws Exception
    {
        Thread.sleep(Long.MAX_VALUE);
    }

    public void stop() throws Exception
    {
        vertxContainer.stop();

        httpServer.close(result -> {
            if (result.succeeded()) {
                logger.info("Server stopped");
            }else{
                logger.error("Failed to stop server", result.cause());
            }
        });
    }

    private static void initializeLogging()
    {
        if (!loggingInitialized.get()) {
            synchronized (UdidbServer.class) {
                if (!loggingInitialized.get()) {
                    System.setProperty("vertx.logger-delegate-factory-class-name",
                            "io.vertx.core.logging.SLF4JLogDelegateFactory");

                    SLF4JBridgeHandler.removeHandlersForRootLogger();

                    SLF4JBridgeHandler.install();

                    loggingInitialized.set(true);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        try {
            UdidbServer server = new UdidbServer(args);

            server.start();
            server.join();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
