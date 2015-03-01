/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import java.util.Arrays;
import java.util.Iterator;

import net.libudi.api.UdiThread;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.files.Executable;
import net.udidb.engine.context.DebuggeeContext;

import static org.mockito.Mockito.*;

/**
 * @author mcnulty
 */
public class DisplayParseTree
{

    private static DebuggeeContext createDebuggeeContext() throws Exception
    {
        DebuggeeContext debuggeeContext = mock(DebuggeeContext.class);
        UdiThread udiThread = mock(UdiThread.class);
        Executable executable = mock(Executable.class);
        Function function = mock(Function.class);

        when(debuggeeContext.getCurrentThread()).thenReturn(udiThread);
        when(debuggeeContext.getExecutable()).thenReturn(executable);

        when(udiThread.getPC()).thenReturn(0xdeadbeefL);

        when(executable.getContainingFunction(0xdeadbeefL)).thenReturn(function);

        return debuggeeContext;
    }

    public static void main(String[] args) throws Exception {
        StringBuilder builder = new StringBuilder();

        Iterator<String> iter = Arrays.asList(args).iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (iter.hasNext()) {
                builder.append(" ");
            }
        }

        CExpressionCompiler compiler = new CExpressionCompiler();
        compiler.compile(builder.toString(), createDebuggeeContext(), true);
    }
}
