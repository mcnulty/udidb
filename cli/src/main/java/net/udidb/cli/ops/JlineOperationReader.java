/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import net.udidb.engine.events.EventDispatcher;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationParseException;
import net.udidb.engine.ops.OperationReader;
import net.udidb.engine.ops.UnknownOperationException;
import net.udidb.engine.ops.parser.OperationParser;

/**
 * Implementation of OperationReader that reads operations from the command line interface using the JLine library
 *
 * @author mcnulty
 */
@Singleton
public class JlineOperationReader implements OperationReader {

    private static final String PROMPT = "(udidb) ";

    private final Terminal terminal;

    private final ConsoleReader reader;

    private final OperationParser parser;

    @Inject
    JlineOperationReader(@Named("INPUT DESTINATION") InputStream in, @Named("OUTPUT DESTINATION") PrintStream out,
            OperationParser parser) throws Exception
    {
        this.terminal = TerminalFactory.create();
        this.terminal.init();
        this.reader = new ConsoleReader(in, out, terminal);
        this.reader.setPrompt(PROMPT);
        this.parser = parser;
    }

    @Override
    public Operation read() throws IOException, UnknownOperationException, OperationParseException {
        Operation cmd = null;
        while (cmd == null) {
            String line = reader.readLine();
            if (line.isEmpty()) continue;

            cmd = parser.parse(line);
        }

        return cmd;
    }
}
