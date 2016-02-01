/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.impls.breakpoints;

import com.google.inject.Inject;

import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.annotations.DisplayName;
import net.udidb.engine.ops.annotations.HelpMessage;
import net.udidb.engine.ops.impls.ContextExpressionOperation;
import net.udidb.engine.ops.results.Result;
import net.udidb.engine.ops.results.ValueResult;
import net.udidb.expr.Expression;
import net.udidb.expr.values.ValueType;

/**
 * An operation to create and install a breakpoint in a debuggee
 *
 * @author mcnulty
 */
@HelpMessage("Set a breakpoint in a debuggee")
@DisplayName("break")
public class SetBreakpoint extends ContextExpressionOperation
{
    // TODO need to add tracking for breakpoints

    @Inject
    SetBreakpoint() {
    }

    @Override
    protected Result executeWithExpression(DebuggeeContext context, Expression expression) throws OperationException
    {
        if (expression.getValue().getType() != ValueType.ADDRESS &&
                expression.getValue().getType() != ValueType.NUMBER)
        {
            throw new OperationException("Expression value cannot be used to set a breakpoint");
        }

        long address = expression.getValue().getAddressValue();
        try {
            context.getProcess().createBreakpoint(address);

            context.getProcess().installBreakpoint(address);
        }catch (UdiException e) {
            throw new OperationException("Failed to set breakpoint in debuggee", e);
        }

        return new ValueResult(String.format("Set breakpoint at 0x%x", address), expression.getValue());
    }
}
