/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver;

import com.google.inject.name.Names;

import net.libudi.api.UdiProcessManager;
import net.libudi.api.event.UdiEventVisitor;
import net.libudi.api.jni.impl.UdiProcessManagerImpl;
import net.sourcecrumbs.api.files.BinaryReader;
import net.sourcecrumbs.refimpl.CrossPlatformBinaryReader;
import net.udidb.cli.ops.events.CliEventDispatcher;
import net.udidb.cli.source.CliSourceLineRowFactory;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextFactory;
import net.udidb.engine.context.GlobalContextManager;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.expr.ExpressionCompilerDelegate;
import net.udidb.engine.ops.parser.ParserModule;
import net.udidb.engine.source.SourceLineRowFactory;
import net.udidb.expr.ExpressionCompiler;

/**
 * @author mcnulty
 */
public class ScenariosModule extends ParserModule
{
    @Override
    protected void configure()
    {
        super.configure();
        bind(String[].class).annotatedWith(Names.named("CUSTOM_IMPL_PACKAGES")).toInstance(
                new String[] {"net.udidb.cli.ops.impl"});

        TestEventVisitor eventVisitor = new TestEventVisitor();

        bind(UdiProcessManager.class).toInstance(new UdiProcessManagerImpl());
        bind(EventDispatcher.class).to(CliEventDispatcher.class);
        bind(UdiEventVisitor.class).toInstance(eventVisitor);
        bind(TestEventVisitor.class).toInstance(eventVisitor);
        bind(BinaryReader.class).toInstance(new CrossPlatformBinaryReader());
        bind(ExpressionCompiler.class).toInstance(new ExpressionCompilerDelegate());
        bind(SourceLineRowFactory.class).toInstance(new CliSourceLineRowFactory());
        bind(DebuggeeContextFactory.class).to(GlobalContextManager.class);
        bind(DebuggeeContext.class).toProvider(GlobalContextManager.class);
    }
}
