/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.cli.ops.impls.display;

import com.google.inject.Inject;

import net.udidb.engine.ops.impls.DisplayNameOperation;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.annotations.LongHelpMessage;
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
@HelpMessage(enMessage="Display the value of an expression")
@LongHelpMessage(enMessage=
        "print <expression>\n\n" +
        "Display the value of an expression"
)
@DisplayName("print")
public class Print extends DisplayNameOperation {

    @Operand(order=0, operandParser = ExpressionParser.class, restOfLine = true)
    private Expression value;

    @Inject
    public Print() {
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(Expression value) {
        this.value = value;
    }

    @Override
    public Result execute()
    {
        if (value.isExpressionCompleted()) {
            return new ValueResult(value.getValue().toString());
        }

        // TODO support execution
        return new ValueResult("(expression value deferred)");
    }
}
