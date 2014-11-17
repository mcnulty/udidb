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

import java.util.List;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import net.libudi.api.exceptions.UdiException;
import net.sourcecrumbs.api.Range;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.files.Executable;
import net.sourcecrumbs.api.symbols.Symbol;
import net.sourcecrumbs.api.debug.symbols.ContextInspectionException;
import net.sourcecrumbs.api.debug.symbols.Variable;
import net.udidb.engine.context.DebuggeeContext;
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
            DebuggeeContext debuggeeContext,
            Function currentFunction,
            long pc)
    {
        super(states);
        this.currentFunction = currentFunction;
        this.pc = pc;
        this.executable = debuggeeContext.getExecutable();
    }

    @Override
    public Void visitPrimaryExpression(@NotNull PrimaryExpressionContext ctx)
    {
        TerminalNode identifierNode = ctx.Identifier();
        if (identifierNode != null) {
            String identifier = identifierNode.getSymbol().getText();

            boolean resolved = false;
            NodeState nodeState = new NodeState();

            // Is it a local variable reference?
            List<Variable> variables = currentFunction.getLocalVariablesByName(identifier);
            if (variables != null && variables.size() > 0) {
                // Locate the variable that is in scope
                for (Variable variable : variables) {
                    Range<Long> scope = variable.getContainingScope();
                    if (scope.getStart() <= pc && pc <= scope.getEnd()) {
                        nodeState.setVariable(variable);
                        resolved = true;
                        break;
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
                Symbol symbol = executable.getSymbol(identifier);
                if (symbol != null) {
                    nodeState.setSymbol(symbol);
                }
            }

            // Couldn't resolve reference
            if (!resolved) {
                throw new ParseCancellationException("Could not locate symbol '" + identifier + "'");
            }

            setNodeState(ctx, nodeState);
        }

        return super.visitPrimaryExpression(ctx);
    }
}
