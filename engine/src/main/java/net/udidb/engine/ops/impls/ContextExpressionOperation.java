/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls;

import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.context.DebuggeeContextAware;
import net.udidb.engine.ops.NoDebuggeeContextException;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.parser.ExpressionParser;
import net.udidb.engine.ops.results.Result;
import net.udidb.expr.Expression;

/**
 * An Operation that requires a DebuggeeContext and takes a single operand that is an Expression
 *
 * @author mcnulty
 */
public abstract class ContextExpressionOperation extends DisplayNameOperation implements DebuggeeContextAware
{
    private DebuggeeContext context;

    @Operand(order=0, operandParser = ExpressionParser.class, restOfLine = true)
    private Expression expression;

    public Expression getExpression()
    {
        return expression;
    }

    public void setExpression(Expression expression)
    {
        this.expression = expression;
    }

    @Override
    public void setDebuggeeContext(DebuggeeContext debuggeeContext)
    {
        this.context = debuggeeContext;
    }

    protected abstract Result executeWithExpression(DebuggeeContext context, Expression expression) throws OperationException;

    @Override
    public Result execute() throws OperationException
    {
        if (context == null) {
            throw new NoDebuggeeContextException();
        }

        return executeWithExpression(context, expression);
    }

}
