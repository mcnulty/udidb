/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.junit.Ignore;
import org.junit.Test;

import net.udidb.engine.context.DebuggeeContext;

/**
 * Unit test for ExpressionSimplificationVisitor
 *
 * @author dmcnulty
 */
public class ExpressionSimplificationVisitorTest
{
    private static ExpressionSimplificationVisitor createVisitor() throws Exception
    {
        DebuggeeContext debuggeeContext = mock(DebuggeeContext.class);
        ParseTreeProperty<NodeState> states = new ParseTreeProperty<>();

        return new ExpressionSimplificationVisitor(states, debuggeeContext);
    }

    @Ignore
    @Test
    public void exploratoryTest() throws Exception
    {
        ExpressionSimplificationVisitor visitor = createVisitor();
        ParserRuleContext parseTree = CExpressionCompiler.createParseTree("1 + 2.0 + 3");
        parseTree.accept(visitor);

        assertEquals(parseTree.getText(), "6.0");
    }
}
