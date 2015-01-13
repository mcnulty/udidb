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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import com.google.common.annotations.VisibleForTesting;

import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.expr.ExpressionException;
import net.udidb.expr.Expression;
import net.udidb.expr.ExpressionCompiler;
import net.udidb.expr.grammar.c.CLexer;
import net.udidb.expr.grammar.c.CParser;

/**
 * An expression compiler for C
 *
 * @author mcnulty
 */
public class CExpressionCompiler implements ExpressionCompiler
{
    @Override
    public Expression compile(String expression, DebuggeeContext debuggeeContext) throws ExpressionException
    {
        long pc;
        try {
            pc = debuggeeContext.getCurrentThread().getPC();
        }catch (UdiException e) {
            throw new ExpressionException(e);
        }
        Function currentFunction = debuggeeContext.getExecutable().getContainingFunction(pc);

        ParseTreeProperty<NodeState> states = new ParseTreeProperty<>();

        // Parse the expression
        ParserRuleContext parseTree;
        try {
            parseTree = createParseTree(expression);
        }catch (IOException e) {
            throw new ExpressionException(e);
        }

        // Resolve all symbols, interrogating the debuggee as necessary
        resolveSymbols(parseTree, states, debuggeeContext, currentFunction, pc);

        // Type checking
        typeCheckExpression(parseTree, states);

        // Simplify the expression given the current state of the AST
        simplifyExpression(parseTree, states, debuggeeContext);

        // Generate code and produce Expression to encapsulate the result
        return generateCode(parseTree, states);
    }

    private static void resolveSymbols(ParserRuleContext parseTree,
                                       ParseTreeProperty<NodeState> states,
                                       DebuggeeContext debuggeeContext,
                                       Function currentFunction,
                                       long pc)
    {
        SymbolResolutionVisitor resolutionVisitor = new SymbolResolutionVisitor(states, debuggeeContext, currentFunction, pc);
        parseTree.accept(resolutionVisitor);
    }

    private static void typeCheckExpression(ParserRuleContext parseTree,
                                            ParseTreeProperty<NodeState> states)
    {
        TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor(states);
        parseTree.accept(typeCheckingVisitor);
    }

    private static void simplifyExpression(ParserRuleContext parseTree,
                                           ParseTreeProperty<NodeState> states,
                                           DebuggeeContext debuggeeContext)
    {
        ExpressionSimplificationVisitor visitor = new ExpressionSimplificationVisitor(states, debuggeeContext);
        parseTree.accept(visitor);
    }

    private static Expression generateCode(ParserRuleContext parseTree,
                                           ParseTreeProperty<NodeState> states)
    {
        return null;
    }

    @VisibleForTesting
    static ParserRuleContext createParseTree(String expression) throws IOException
    {
        ANTLRInputStream inputStream = new ANTLRInputStream(new ByteArrayInputStream(expression.getBytes(StandardCharsets.UTF_8)));
        CLexer lexer = new CLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        CParser parser = new CParser(tokens);
        return parser.expression();
    }
}
