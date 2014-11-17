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
        ParserRuleContext parseTree = CExpressionEvaluator.createParseTree("(value + 1)*3 + increment - min(value, increment)");

        parseTree.accept(symbolResolutionVisitor);
    }
}
