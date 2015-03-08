/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Test;

import net.libudi.api.UdiThread;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.files.Executable;
import net.udidb.expr.ExecutionContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for ExpressionSimplificationVisitor
 *
 * @author dmcnulty
 */
public class ExpressionSimplificationVisitorTest
{
    private ExecutionContext createDefaultExecutionContext() throws Exception
    {
        ExecutionContext executionContext = mock(ExecutionContext.class);
        UdiThread currentThread = mock(UdiThread.class);
        Function currentFunction = mock(Function.class);
        Executable executable = mock(Executable.class);

        when(currentThread.getPC()).thenReturn(0L);

        when(executable.getContainingFunction(0L)).thenReturn(currentFunction);

        when(executionContext.getCurrentThread()).thenReturn(currentThread);
        when(executionContext.getExecutable()).thenReturn(executable);

        return executionContext;
    }

    private void runSimplification(ParserRuleContext parseTree,
                                   ParseTreeProperty<NodeState> states,
                                   ExecutionContext executionContext) throws Exception
    {

        long pc = executionContext.getCurrentThread().getPC();
        Function currentFunction = executionContext.getExecutable().getContainingFunction(pc);

        CExpressionCompiler.resolveSymbols(parseTree, states, executionContext, currentFunction, pc);

        CExpressionCompiler.typeCheckExpression(parseTree, states);

        CExpressionCompiler.simplifyExpression(parseTree, states, executionContext);
    }

    private void testSimplification(String input, String output) throws Exception
    {
        testSimplification(input, output, createDefaultExecutionContext());
    }

    private void testSimplification(String input, String output, ExecutionContext executionContext) throws Exception
    {
        ParseTreeProperty<NodeState> states = new ParseTreeProperty<>();

        ParserRuleContext parseTree = CExpressionCompiler.createParseTree(input);

        runSimplification(parseTree, states, executionContext);

        NodeState nodeState = states.get(parseTree);
        assertNotNull(nodeState);
        assertNotNull(nodeState.getExpressionValue());
        assertEquals(nodeState.getExpressionValue().toString(), output);
    }

    @Test
    public void constantsTest() throws Exception
    {
        testSimplification("1", "1");
    }

    @Test
    public void arithmeticTest() throws Exception
    {
        testSimplification("1 + 2.0 + 3", "6.0");
    }
}
