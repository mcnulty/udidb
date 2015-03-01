/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.expr.DeferredExpression;
import net.udidb.expr.Expression;
import net.udidb.expr.FixedExpression;
import net.udidb.expr.grammar.c.CParser.ExpressionContext;

/**
 * Visitor that generates code to evaluate the expression, if necessary. This visitor is also responsible for producing
 * the final Expression object, given the annotated parse tree.
 *
 * @author mcnulty
 */
public class CodeGenVisitor extends BaseExpressionVisitor<Expression>
{
    private final DebuggeeContext debuggeeContext;

    public CodeGenVisitor(ParseTreeProperty<NodeState> states, DebuggeeContext debuggeeContext)
    {
        super(states);
        this.debuggeeContext = debuggeeContext;
    }

    @Override
    public Expression visitExpression(@NotNull ExpressionContext ctx)
    {
        // Check if code generation is required
        NodeState rootState = getNodeState(ctx);
        if (rootState.getExpressionValue() != null) {
            return new FixedExpression(rootState.getExpressionValue());
        }

        // Perform code generation
        super.visitExpression(ctx);

        // TODO this constructor will require data from the code gen process
        return new DeferredExpression();
    }
}
