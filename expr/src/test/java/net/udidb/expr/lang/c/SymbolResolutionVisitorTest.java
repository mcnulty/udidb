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
import org.junit.Ignore;
import org.junit.Test;

import net.sourcecrumbs.api.debug.symbols.Function;
import net.udidb.engine.context.DebuggeeContext;

import static org.mockito.Mockito.*;

/**
 * Unit test for SymbolResolutionVisitor
 *
 * @author mcnulty
 */
public class SymbolResolutionVisitorTest
{
    private static SymbolResolutionVisitor createVisitor() throws Exception
    {
        DebuggeeContext debuggeeContext = mock(DebuggeeContext.class);
        Function currentFunction = mock(Function.class);
        ParseTreeProperty<NodeState> states = new ParseTreeProperty<>();

        return new SymbolResolutionVisitor(states, debuggeeContext, currentFunction, 0);
    }

    @Ignore
    @Test
    public void exploratoryTest() throws Exception
    {
        SymbolResolutionVisitor symbolResolutionVisitor = createVisitor();
        ParserRuleContext parseTree = CExpressionCompiler.createParseTree(
            "(value + 1)*3 + increment - min(value, increment)");

        parseTree.accept(symbolResolutionVisitor);
    }
}
