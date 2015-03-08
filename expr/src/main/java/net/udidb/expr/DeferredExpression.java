/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr;

import net.udidb.expr.values.ExpressionValue;

/**
 * An Expression implementation that requires execution in the debuggee to produce the final value
 *
 * @author mcnulty
 */
public class DeferredExpression implements Expression
{
    @Override
    public ExpressionValue getValue()
    {
        return null;
    }

    @Override
    public void loadExpression(ExecutionContext executionContext) throws ExpressionException
    {
        throw new ExpressionException("Debuggee expression evaluation is not yet implemented");
    }

    @Override
    public boolean isExpressionCompleted()
    {
        return false;
    }
}
