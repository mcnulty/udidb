/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.gui.TreeTextProvider;
import org.antlr.v4.runtime.tree.gui.TreeViewer;
import org.antlr.v4.runtime.tree.gui.TreeViewer.DefaultTreeTextProvider;

import com.google.common.annotations.VisibleForTesting;

import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.udidb.expr.ExecutionContext;
import net.udidb.expr.ExpressionException;
import net.udidb.expr.Expression;
import net.udidb.expr.ExpressionCompiler;
import net.udidb.expr.grammar.c.CLexer;
import net.udidb.expr.grammar.c.CParser;
import net.udidb.expr.values.ExpressionValue;

/**
 * An expression compiler for C
 *
 * @author mcnulty
 */
public class CExpressionCompiler implements ExpressionCompiler
{
    @Override
    public Expression compile(String expression, ExecutionContext executionContext) throws ExpressionException
    {
        return compile(expression, executionContext, false);
    }

    @VisibleForTesting
    Expression compile(String expression, ExecutionContext executionContext, boolean displayTree) throws ExpressionException
    {
        long pc;
        try {
            pc = executionContext.getCurrentThread().getPC();
        } catch (UdiException e) {
            throw new ExpressionException(e);
        }
        Function currentFunction = executionContext.getExecutable().getContainingFunction(pc);

        final ParseTreeProperty<NodeState> states = new ParseTreeProperty<>();

        // Parse the expression
        final CParser parser;
        try {
            parser = createParser(expression);
        }catch (IOException e) {
            throw new ExpressionException(e);
        }
        ParserRuleContext parseTree = parser.expression();

        // Resolve all symbols, interrogating the debuggee as necessary
        resolveSymbols(parseTree, states, executionContext, currentFunction, pc);

        // Type checking
        typeCheckExpression(parseTree, states);

        // Simplify the expression given the current state of the AST
        simplifyExpression(parseTree, states, executionContext);

        // Generate code and produce Expression to encapsulate the result
        Expression output = generateCode(parseTree, states, executionContext);

        if (displayTree) {
            TreeViewer viewer = new TreeViewer(Arrays.asList(parser.getRuleNames()), parseTree);
            viewer.setTreeTextProvider(new TreeTextProvider()
            {
                TreeTextProvider defaultProvider = new DefaultTreeTextProvider(Arrays.asList(parser.getRuleNames()));

                @Override
                public String getText(Tree node)
                {
                    if (node instanceof ParseTree) {
                        NodeState nodeState = states.get((ParseTree)node);

                        ExpressionValue value;
                        if (nodeState != null) {
                            value = nodeState.getExpressionValue();
                        }else{
                            value = null;
                        }

                        if (value != null) {
                            return defaultProvider.getText(node) + "(" + value + ")";
                        }else{
                            return defaultProvider.getText(node) + "(null)";
                        }
                    }

                    return defaultProvider.getText(node);
                }
            });
            viewer.open();
        }

        return output;
    }

    @VisibleForTesting
    static void resolveSymbols(ParserRuleContext parseTree,
                                       ParseTreeProperty<NodeState> states,
                                       ExecutionContext executionContext,
                                       Function currentFunction,
                                       long pc)
    {
        SymbolResolutionVisitor resolutionVisitor = new SymbolResolutionVisitor(states, executionContext, currentFunction, pc);
        parseTree.accept(resolutionVisitor);
    }

    @VisibleForTesting
    static void typeCheckExpression(ParserRuleContext parseTree,
                                            ParseTreeProperty<NodeState> states)
    {
        TypeCheckingVisitor typeCheckingVisitor = new TypeCheckingVisitor(states);
        parseTree.accept(typeCheckingVisitor);
    }

    @VisibleForTesting
    static void simplifyExpression(ParserRuleContext parseTree,
                                           ParseTreeProperty<NodeState> states,
                                           ExecutionContext executionContext)
    {
        ExpressionSimplificationVisitor visitor = new ExpressionSimplificationVisitor(states, executionContext);
        parseTree.accept(visitor);
    }

    @VisibleForTesting
    static Expression generateCode(ParserRuleContext parseTree,
                                           ParseTreeProperty<NodeState> states,
                                           ExecutionContext executionContext)
    {
        CodeGenVisitor visitor = new CodeGenVisitor(states, executionContext);
        return parseTree.accept(visitor);
    }

    private static CParser createParser(String expression) throws IOException
    {
        ANTLRInputStream inputStream = new ANTLRInputStream(new ByteArrayInputStream(expression.getBytes(StandardCharsets.UTF_8)));
        CLexer lexer = new CLexer(inputStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new CParser(tokens);
    }

    @VisibleForTesting
    static ParserRuleContext createParseTree(String expression) throws IOException
    {
        return createParser(expression).expression();
    }
}
