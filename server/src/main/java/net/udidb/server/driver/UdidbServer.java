/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import net.udidb.server.api.resources.DebuggeeContexts;
import net.udidb.server.web.ExceptionHandler;
import net.udidb.server.web.LoggingHandler;
import net.udidb.server.web.PathParamContextHandler;
import net.udidb.server.web.PathParamHandler;
import net.udidb.server.web.VarPathParamHandler;

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

    public UdidbServer(String[] args)
    {
        // TODO process args to configure the server

        initializeLogging();

        String uiPath = System.getProperty("udidb.ui.path", "");
        boolean cors = Boolean.getBoolean("udidb.cors");

        Injector injector = Guice.createInjector(new ServerModule());

        Vertx vertx = injector.getInstance(Vertx.class);

        this.httpServer = vertx.createHttpServer(new HttpServerOptions().setWebsocketSubProtocols("wamp.2.json"));

        // WebSocket events
        this.httpServer.websocketHandler(websocket -> {
            if (!websocket.path().equals("/events")) {
                websocket.reject();
            }

            injector.getInstance(EventsSocket.class).setServerWebSocket(websocket);
        });

        Router router = Router.router(vertx);

        // static content for the UI
        StaticHandler staticHandler = StaticHandler.create();
        if (uiPath != null) {
            staticHandler.setAllowRootFileSystemAccess(true);
            staticHandler.setWebRoot(uiPath);
        }else{
            staticHandler.setWebRoot("webui");
        }
        router.route("/webui/*").handler(staticHandler);

        // API resources
        if (cors) {
            router.route().handler(CorsHandler.create("*").allowedHeader("Content-Type"));
        }

        router.route().handler(BodyHandler.create());

        DebuggeeContexts debuggeeContexts = injector.getInstance(DebuggeeContexts.class);

        router.get("/debuggeeContexts")
              .blockingHandler(noParamHandler(debuggeeContexts::getAll));
        router.post("/debuggeeContexts")
              .blockingHandler(bodyHandler(debuggeeContexts::create));
        router.options("/debuggeeContexts")
              .blockingHandler(ok());
        router.get("/debuggeeContexts/operations")
              .blockingHandler(noParamHandler(debuggeeContexts::getOperationDescriptions));
        router.post("/debuggeeContexts/globalOperation")
              .blockingHandler(bodyHandler(debuggeeContexts::createGlobalOperation));
        router.options("/debuggeeContexts/globalOperation")
              .blockingHandler(ok());
        router.get("/debuggeeContexts/:id")
              .blockingHandler(pathParamHandler("id", debuggeeContexts::get));
        router.get("/debuggeeContexts/:id/process")
              .blockingHandler(pathParamHandler("id", debuggeeContexts::getProcess));
        router.get("/debuggeeContexts/:id/process/threads")
              .blockingHandler(pathParamHandler("id", debuggeeContexts::getThreads));
        router.get("/debuggeeContexts/:id/process/threads/:threadId")
              .blockingHandler(varPathParamHandler(debuggeeContexts::getThread, "id", "threadId"));
        router.post("/debuggeeContexts/:id/process/operation")
              .blockingHandler(bodyHandler("id", debuggeeContexts::createOperation));
        router.options("/debuggeeContexts/:id/process/operation")
              .blockingHandler(ok());
        router.get("/debuggeeContexts/:id/process/operation")
              .blockingHandler(pathParamHandler("id", debuggeeContexts::getOperation));
        router.get("/debuggeeContexts/:id/process/operations")
              .blockingHandler(pathParamHandler("id", debuggeeContexts::getOperationDescriptions));

        httpServer.requestHandler(router::accept);
    }

    private static Handler<RoutingContext> defaultHandler(Handler<RoutingContext> handler)
    {
        if (LoggingHandler.isEnabled()) {
            return new ExceptionHandler(new LoggingHandler(handler));
        } else {
            return new ExceptionHandler(handler);
        }
    }

    private static Handler<RoutingContext> noParamHandler(Consumer<HttpServerResponse> handler)
    {
        return defaultHandler(context -> handler.accept(context.response()));
    }

    private static Handler<RoutingContext> pathParamHandler(String paramName,
                                                            BiConsumer<HttpServerResponse, String> handler)
    {
        return defaultHandler(new PathParamHandler(paramName, handler));
    }

    private static Handler<RoutingContext> varPathParamHandler(BiConsumer<HttpServerResponse, List<String>> handler,
                                                               String paramName,
                                                               String... paramNames)
    {
        return defaultHandler(new VarPathParamHandler(handler, paramName, paramNames));
    }

    private static Handler<RoutingContext> bodyHandler(BiConsumer<HttpServerResponse, String> handler)
    {
        return defaultHandler(context -> handler.accept(context.response(), context.getBodyAsString()));
    }

    private static Handler<RoutingContext> bodyHandler(String paramName, BiConsumer<RoutingContext, String> handler)
    {
        return defaultHandler(new PathParamContextHandler(paramName, handler));
    }

    private static Handler<RoutingContext> ok()
    {
        return defaultHandler(context -> context.response().setStatusCode(HttpResponseStatus.OK.code()).end());
    }

    public void start() throws Exception
    {
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
            throw e;
        }
    }
}
