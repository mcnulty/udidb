/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.query;

import com.google.inject.Inject;

import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.Operand;
import net.udidb.engine.ops.parser.ExpressionParser;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.expr.Expression;

/**
 * An operation to display an expression
 *
 * @author mcnulty
 */
@HelpMessage("Display the value of an expression")
@DisplayName("eval")
public class Eval extends DisplayNameOperation {

    @Operand(order=0, operandParser = ExpressionParser.class, restOfLine = true)
    private Expression expression;

    @Inject
    public Eval() {
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Result execute()
    {
        if (expression.isExpressionCompleted()) {
            return new ValueResult(expression.getValue().toString(), expression.getValue());
        }

        // TODO support execution
        return new ValueResult("(expression value deferred)");
    }
}
