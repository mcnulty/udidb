/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.driver;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import net.libudi.api.UdiProcessManager;
import net.libudi.api.event.UdiEventVisitor;
import net.libudi.api.jni.impl.UdiProcessManagerImpl;
import net.sourcecrumbs.api.files.BinaryReader;
import net.sourcecrumbs.refimpl.CrossPlatformBinaryReader;
import net.udidb.cli.ops.CliResultVisitor;
import net.udidb.cli.ops.JlineOperationReader;
import net.udidb.cli.context.GlobalContextManager;
import net.udidb.cli.ops.events.CliEventDispatcher;
import net.udidb.cli.ops.events.CliEventVisitor;
import net.udidb.cli.ops.impls.help.HelpMessageProvider;
import net.udidb.cli.ops.impls.internals.SetStackTrace;
import net.udidb.cli.ops.impls.internals.ShowInternals;
import net.udidb.cli.source.CliSourceLineRowFactory;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.expr.ExpressionCompilerDelegate;
import net.udidb.engine.ops.OperationReader;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextFactory;
import net.udidb.engine.ops.impls.Setting;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.parser.ParserModule;
import net.udidb.engine.source.SourceLineRowFactory;
import net.udidb.expr.ExpressionCompiler;

/**
 * A Guice module defining dependencies for running the debugger from the command line
 *
 * @author mcnulty
 */
public class CliModule extends ParserModule {

    @Override
    protected void configure() {
        super.configure();

        bind(OperationReader.class).to(JlineOperationReader.class);

        bind(OperationResultVisitor.class).to(CliResultVisitor.class);

        bind(PrintStream.class).annotatedWith(Names.named("OUTPUT DESTINATION")).toInstance(System.out);

        bind(InputStream.class).annotatedWith(Names.named("INPUT DESTINATION")).toInstance(System.in);

        bind(String[].class).annotatedWith(Names.named("CUSTOM_IMPL_PACKAGES")).toInstance(
                new String[]{ "net.udidb.cli.ops.impl" });

        bind(DebuggeeContextFactory.class).to(GlobalContextManager.class);

        bind(DebuggeeContext.class).toProvider(GlobalContextManager.class);

        bind(EventDispatcher.class).to(CliEventDispatcher.class);

        bind(UdiEventVisitor.class).to(CliEventVisitor.class);

        bind(HelpMessageProvider.class).asEagerSingleton();

        bind(UdiProcessManager.class).toInstance(new UdiProcessManagerImpl());

        bind(BinaryReader.class).toInstance(new CrossPlatformBinaryReader());

        bind(ExpressionCompiler.class).toInstance(new ExpressionCompilerDelegate());

        bind(SourceLineRowFactory.class).toInstance(new CliSourceLineRowFactory());
    }

    @Inject
    @Provides
    public ShowInternals providesShowInternals(
            SetStackTrace setStackTrace
    )
    {
        List<Setting> internalSettings = new ArrayList<>();
        internalSettings.add(setStackTrace);

        return new ShowInternals(internalSettings);
    }
}
