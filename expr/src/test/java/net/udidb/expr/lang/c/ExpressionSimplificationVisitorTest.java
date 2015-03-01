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
import net.udidb.engine.context.DebuggeeContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for ExpressionSimplificationVisitor
 *
 * @author dmcnulty
 */
public class ExpressionSimplificationVisitorTest
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

    private void runSimplification(ParserRuleContext parseTree,
                                   ParseTreeProperty<NodeState> states,
                                   DebuggeeContext debuggeeContext) throws Exception
    {

        long pc = debuggeeContext.getCurrentThread().getPC();
        Function currentFunction = debuggeeContext.getExecutable().getContainingFunction(pc);

        CExpressionCompiler.resolveSymbols(parseTree, states, debuggeeContext, currentFunction, pc);

        CExpressionCompiler.typeCheckExpression(parseTree, states);

        CExpressionCompiler.simplifyExpression(parseTree, states, debuggeeContext);
    }

    private void testSimplification(String input, String output) throws Exception
    {
        testSimplification(input, output, createDefaultDebuggeeContext());
    }

    private void testSimplification(String input, String output, DebuggeeContext debuggeeContext) throws Exception
    {
        ParseTreeProperty<NodeState> states = new ParseTreeProperty<>();

        ParserRuleContext parseTree = CExpressionCompiler.createParseTree(input);

        runSimplification(parseTree, states, debuggeeContext);

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
