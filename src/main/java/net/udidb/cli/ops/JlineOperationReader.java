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

package net.udidb.cli.ops;

import java.io.IOException;

import com.google.inject.Inject;

import jline.Terminal;
import jline.TerminalFactory;
import jline.console.ConsoleReader;
import net.udidb.engine.ops.Operation;
import net.udidb.engine.ops.OperationReader;
import net.udidb.engine.ops.info.Echo;

/**
 * Implementation of OperationReader that reads operations from the command line interface using the JLine library
 *
 * @author mcnulty
 */
public class JlineOperationReader implements OperationReader {

    private static final String PROMPT = "(udidb) ";

    private final Terminal terminal;

    private final ConsoleReader reader;

    @Inject
    JlineOperationReader() throws Exception {
        terminal = TerminalFactory.create();
        terminal.init();
        reader = new ConsoleReader(System.in, System.out, terminal);
        reader.setPrompt(PROMPT);
    }

    @Override
    public Operation read() throws IOException {
        Echo cmd = new Echo();
        cmd.setValue(reader.readLine());

        return cmd;
    }
}
