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

import net.libudi.api.UdiProcess;
import net.libudi.api.UdiThread;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.files.Executable;
import net.udidb.expr.ExecutionContext;
import net.udidb.expr.Expression;
import net.udidb.expr.values.ExpressionValue;

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
    private ExecutionContext createDefaultExecutionContext(boolean waitingForState) throws Exception
    {
        ExecutionContext executionContext = mock(ExecutionContext.class);
        UdiThread currentThread = mock(UdiThread.class);
        Function currentFunction = mock(Function.class);
        Executable executable = mock(Executable.class);
        UdiProcess parentProcess = mock(UdiProcess.class);

        when(parentProcess.isWaitingForStart()).thenReturn(waitingForState);

        when(currentThread.getPC()).thenReturn(0L);
        when(currentThread.getParentProcess()).thenReturn(parentProcess);

        when(executable.getContainingFunction(0L)).thenReturn(currentFunction);

        when(executionContext.getCurrentThread()).thenReturn(currentThread);
        when(executionContext.getExecutable()).thenReturn(executable);

        return executionContext;
    }

    private void functionTest(ExecutionContext executionContext) throws Exception
    {
        long entryAddress = 0xc0ffeeabcdL;

        CExpressionCompiler compiler = new CExpressionCompiler();

        Executable executable = executionContext.getExecutable();

        Function main = mock(Function.class);
        when(main.getName()).thenReturn("main");
        when(main.getEntryAddress()).thenReturn(entryAddress);
        when(main.getReturnType()).thenReturn(Types.getType("int"));

        when(executable.getFunction(main.getName())).thenReturn(main);

        Expression expression = compiler.compile(main.getName(), executionContext);
        assertTrue(expression.isExpressionCompleted());

        ExpressionValue value = expression.getValue();
        assertNotNull(value);
        assertEquals(entryAddress, value.getAddressValue());
    }

    @Test
    public void functionTest() throws Exception
    {
        functionTest(createDefaultExecutionContext(false));
    }

    @Test
    public void functionTestWaitingForState() throws Exception
    {
        functionTest(createDefaultExecutionContext(true));
    }
}
