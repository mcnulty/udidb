/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.driver;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import net.libudi.api.UdiProcessManager;
import net.libudi.api.jni.impl.UdiProcessManagerImpl;
import net.sourcecrumbs.api.files.BinaryReader;
import net.sourcecrumbs.refimpl.CrossPlatformBinaryReader;
import net.udidb.engine.context.DebuggeeContextManager;
import net.udidb.engine.context.DebuggeeContextManagerImpl;
import net.udidb.engine.expr.ExpressionCompilerDelegate;
import net.udidb.engine.ops.impls.help.HelpMessageProvider;
import net.udidb.engine.source.InMemorySourceLineRowFactory;
import net.udidb.engine.source.SourceLineRowFactory;
import net.udidb.expr.ExpressionCompiler;

/**
 * A Guice module defining dependencies for running the debugger within a server
 *
 * @author mcnulty
 */
public class ServerModule extends AbstractModule
{

    @Override
    protected void configure()
    {
        bind(String[].class).annotatedWith(Names.named("OP_PACKAGES")).toInstance(
                new String[] {
                        "net.udidb.engine.ops.impls",
                        "net.udidb.cli.ops.impl"
                });

        bind(DebuggeeContextManager.class).to(DebuggeeContextManagerImpl.class);

        bind(HelpMessageProvider.class).asEagerSingleton();

        bind(UdiProcessManager.class).toInstance(new UdiProcessManagerImpl());

        bind(BinaryReader.class).toInstance(new CrossPlatformBinaryReader());

        bind(ExpressionCompiler.class).toInstance(new ExpressionCompilerDelegate());

        bind(SourceLineRowFactory.class).toInstance(new InMemorySourceLineRowFactory());
    }
}
