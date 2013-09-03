/*
 * Copyright (c) 2011-2013, Dan McNulty
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the UDI project nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */

package net.udidb.cli.driver;

import java.io.InputStream;
import java.io.PrintStream;

import com.google.inject.Inject;
import com.google.inject.name.Names;

import net.udidb.cli.ops.CliResultVisitor;
import net.udidb.cli.ops.JlineOperationReader;
import net.udidb.cli.ops.impls.config.context.GlobalContextManager;
import net.udidb.engine.ops.OperationReader;
import net.udidb.engine.ops.impls.control.DebuggeeContext;
import net.udidb.engine.ops.impls.control.DebuggeeContextFactory;
import net.udidb.engine.ops.results.OperationResultVisitor;
import net.udidb.engine.ops.parser.ParserModule;

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
    }
}
