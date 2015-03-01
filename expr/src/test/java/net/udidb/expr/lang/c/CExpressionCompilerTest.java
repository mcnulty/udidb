/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import org.junit.Test;

import net.libudi.api.UdiThread;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.files.Executable;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.expr.Expression;
import net.udidb.expr.ExpressionValue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit test for CExpressionCompiler
 *
 * @author mcnulty
 */
public class CExpressionCompilerTest
{
    private DebuggeeContext createDefaultDebuggeeContext() throws Exception
    {
        DebuggeeContext debuggeeContext = mock(DebuggeeContext.class);
        UdiThread currentThread = mock(UdiThread.class);
        Function currentFunction = mock(Function.class);
        Executable executable = mock(Executable.class);

        when(currentThread.getPC()).thenReturn(0L);

        when(executable.getContainingFunction(0L)).thenReturn(currentFunction);

        when(debuggeeContext.getCurrentThread()).thenReturn(currentThread);
        when(debuggeeContext.getExecutable()).thenReturn(executable);

        return debuggeeContext;
    }

    @Test
    public void functionTest() throws Exception
    {
        long entryAddress = 0xc0ffeeabcdL;

        CExpressionCompiler compiler = new CExpressionCompiler();

        DebuggeeContext debuggeeContext = createDefaultDebuggeeContext();
        Executable executable = debuggeeContext.getExecutable();

        Function main = mock(Function.class);
        when(main.getName()).thenReturn("main");
        when(main.getEntryAddress()).thenReturn(entryAddress);
        when(main.getReturnType()).thenReturn(Types.getType("int"));

        when(executable.getFunction(main.getName())).thenReturn(main);

        Expression expression = compiler.compile(main.getName(), debuggeeContext);
        assertTrue(expression.isExpressionCompleted(debuggeeContext));

        ExpressionValue value = expression.getValue();
        assertNotNull(value);
        assertEquals(entryAddress, value.getAddressValue());
    }
}
