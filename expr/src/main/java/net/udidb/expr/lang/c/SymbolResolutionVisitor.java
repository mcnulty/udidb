/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import net.sourcecrumbs.api.Range;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.files.Executable;
import net.sourcecrumbs.api.symbols.Symbol;
import net.sourcecrumbs.api.debug.symbols.Variable;
import net.udidb.expr.ExecutionContext;
import net.udidb.expr.grammar.c.CParser.PrimaryExpressionContext;

/**
 * Visitor that resolves symbol references
 *
 * @author mcnulty
 */
public class SymbolResolutionVisitor extends BaseExpressionVisitor<Void>
{
    private final Function currentFunction;
    private final long pc;
    private final Executable executable;

    public SymbolResolutionVisitor(ParseTreeProperty<NodeState> states,
            ExecutionContext executionContext,
            Function currentFunction,
            long pc)
    {
        super(states);
        this.currentFunction = currentFunction;
        this.pc = pc;
        this.executable = executionContext.getExecutable();
    }

    @Override
    public Void visitPrimaryExpression(@NotNull PrimaryExpressionContext ctx)
    {
        TerminalNode identifierNode = ctx.Identifier();
        if (identifierNode != null) {
            String identifier = identifierNode.getSymbol().getText();

            boolean resolved = false;
            NodeState nodeState = getNodeState(ctx);

            if (currentFunction != null) {
                // Is it a local variable reference?
                List<Variable> variables = currentFunction.getLocalVariablesByName(identifier);
                if (variables != null && variables.size() > 0) {
                    // Locate the variable that is in scope
                    for (Variable variable : variables) {
                        Range<Long> scope = variable.getScope();
                        if (scope.getStart() <= pc && pc <= scope.getEnd()) {
                            nodeState.setVariable(variable);
                            resolved = true;
                            break;
                        }
                    }
                }
            }

            // Is it a function reference?
            if (!resolved) {
                Function targetFunction = executable.getFunction(identifier);
                if (targetFunction != null) {
                    nodeState.setFunction(targetFunction);
                    resolved = true;
                }
            }

            // Is it a global variable reference?
            if (!resolved) {
                Variable variable = executable.getGlobalVariable(identifier);
                if (variable != null) {
                    nodeState.setVariable(variable);
                    resolved = true;
                }
            }

            // Is it a generic symbol reference?
            if (!resolved) {
                List<Symbol> symbols = executable.getSymbolsByName(identifier);
                if (symbols != null && symbols.size() > 0) {
                    // TODO configurable strategy for picking the symbol to use
                    nodeState.setSymbol(symbols.get(0));
                }
            }

            // Couldn't resolve reference
            if (!resolved) {
                throw new ParseCancellationException("Could not locate symbol '" + identifier + "'");
            }
        }

        return super.visitPrimaryExpression(ctx);
    }
}
