/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import java.util.Collections;
import java.util.logging.Logger;

import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;

import org.glassfish.hk2.api.Context;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.jersey.filter.LoggingFilter;
import org.jvnet.hk2.guice.bridge.api.GuiceBridge;
import org.jvnet.hk2.guice.bridge.api.GuiceIntoHK2Bridge;
import org.jvnet.hk2.guice.bridge.api.GuiceScope;
import org.jvnet.hk2.guice.bridge.internal.GuiceScopeContext;

import com.englishtown.vertx.guice.GuiceJerseyBinder;
import com.englishtown.vertx.jersey.JerseyOptions;
import com.englishtown.vertx.jersey.impl.DefaultJerseyOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxFactoryImpl;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import net.libudi.api.UdiProcessManager;
import net.libudi.api.jni.impl.UdiProcessManagerImpl;
import net.sourcecrumbs.api.files.BinaryReader;
import net.sourcecrumbs.refimpl.CrossPlatformBinaryReader;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.context.DebuggeeContextManagerImpl;
import net.udidb.engine.expr.ExpressionCompilerDelegate;
import net.udidb.engine.ops.impls.help.HelpMessageProvider;
import net.udidb.engine.ops.results.DeferredResult;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.results.TableResult;
import net.udidb.engine.ops.results.TableRow;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.engine.ops.results.VoidResult;
import net.udidb.engine.source.InMemorySourceLineRowFactory;
import net.udidb.engine.source.SourceLineRowFactory;
import net.udidb.expr.ExpressionCompiler;
import net.udidb.expr.values.ExpressionValue;
import net.udidb.server.api.resources.DebuggeeContexts;
import net.udidb.server.api.results.ExpressionValueSerializer;
import net.udidb.server.api.results.DeferredResultMixIn;
import net.udidb.server.api.results.TableResultMixIn;
import net.udidb.server.api.results.TableRowMixIn;
import net.udidb.server.api.results.ValueResultMixIn;
import net.udidb.server.api.results.VoidResultMixIn;
import net.udidb.server.engine.OperationEngine;
import net.udidb.server.engine.ServerEngine;
import net.udidb.server.engine.ServerEngineImpl;
import net.udidb.server.engine.ServerEventDispatcher;
import net.udidb.server.wamp.InMemoryConnectorProvider;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampRouter;
import ws.wamp.jawampa.WampRouterBuilder;

/**
 * A Guice module defining dependencies for running the debugger within a server
 *
 * @author mcnulty
 */
public class ServerModule extends AbstractModule
{
    private static final Logger logger = Logger.getLogger("net.udidb.server.http.logger");

    private static final String WAMP_REALM = "udidb";
    private static final String INTERNAL_CLIENT_URI = "udidb://wamp";

    @Override
    protected void configure()
    {
        // JSON configuration
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(ExpressionValue.class, new ExpressionValueSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.addMixInAnnotations(VoidResult.class, VoidResultMixIn.class);
        objectMapper.addMixInAnnotations(DeferredResult.class, DeferredResultMixIn.class);
        objectMapper.addMixInAnnotations(TableResult.class, TableResultMixIn.class);
        objectMapper.addMixInAnnotations(ValueResult.class, ValueResultMixIn.class);
        objectMapper.addMixInAnnotations(TableRow.class, TableRowMixIn.class);
        objectMapper.registerModule(simpleModule);
        bind(ObjectMapper.class).toInstance(objectMapper);

        // REST API configuration
        JsonObject jerseyConfig = new JsonObject();
        jerseyConfig.put("guice_binder", ServerModule.class.getCanonicalName());
        jerseyConfig.put("base_path", "/");
        jerseyConfig.put("resources", new JsonArray(Collections.<String>singletonList(DebuggeeContexts.class.getPackage().getName())));
        DefaultJerseyOptions jerseyOptions = new DefaultJerseyOptions(jerseyConfig);

        Vertx vertx = new VertxFactoryImpl().vertx();
        bind(Vertx.class).toInstance(vertx);
        install(Modules.override(new GuiceJerseyBinder()).with(new JerseyOverride(jerseyOptions)));
        LoggingFilter loggingFilter = new LoggingFilter(logger, true);
        Multibinder.newSetBinder(binder(), ContainerRequestFilter.class).addBinding().toInstance(loggingFilter);
        Multibinder.newSetBinder(binder(), ContainerResponseFilter.class).addBinding().toInstance(loggingFilter);

        // Engine configuration
        bind(String[].class).annotatedWith(Names.named("OP_PACKAGES")).toInstance(
                new String[] {
                        "net.udidb.engine.ops.impls"
                });

        bind(DebuggeeContextManager.class).to(DebuggeeContextManagerImpl.class);

        bind(HelpMessageProvider.class).asEagerSingleton();

        bind(UdiProcessManager.class).toInstance(new UdiProcessManagerImpl());

        bind(BinaryReader.class).toInstance(new CrossPlatformBinaryReader());

        bind(ExpressionCompiler.class).toInstance(new ExpressionCompilerDelegate());

        bind(SourceLineRowFactory.class).toInstance(new InMemorySourceLineRowFactory());

        bind(ServerEngine.class).to(ServerEngineImpl.class);

        bind(OperationResultVisitor.class).to(OperationEngine.class);

        bind(ServerEventDispatcher.class).asEagerSingleton();

        WampRouter wampRouter = configureWampRouter();
        bind(WampRouter.class).toInstance(wampRouter);

        bind(WampClient.class).toInstance(configureWampClient(wampRouter));

    }

    private static class JerseyOverride implements Module
    {
        private final JerseyOptions jerseyOptions;

        public JerseyOverride(JerseyOptions jerseyOptions)
        {
            this.jerseyOptions = jerseyOptions;
        }

        @Override
        public void configure(Binder binder)
        {
            binder.bind(JerseyOptions.class).toInstance(jerseyOptions);
        }
    }

    private WampRouter configureWampRouter()
    {
        try {
            return new WampRouterBuilder()
                    .addRealm(WAMP_REALM)
                    .build();
        }catch (ApplicationError e) {
            // TODO there might be a better way to communicate this error
            throw new RuntimeException(e);
        }
    }

    private WampClient configureWampClient(WampRouter wampRouter)
    {
        try {
            WampClient wampClient = new WampClientBuilder()
                    .withRealm(WAMP_REALM)
                    .withUri(INTERNAL_CLIENT_URI)
                    .withConnectorProvider(new InMemoryConnectorProvider(wampRouter))
                    .build();
            wampClient.open();
            return wampClient;
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
