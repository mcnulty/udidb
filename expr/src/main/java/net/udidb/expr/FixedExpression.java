/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr;

import net.udidb.engine.context.DebuggeeContext;

/**
 * An Expression implementation for which the value is known at construction time
 *
 * @author mcnulty
 */
public class FixedExpression implements Expression
{
    private final ExpressionValue value;

    public FixedExpression(ExpressionValue value)
    {
        this.value = value;
    }

    @Override
    public ExpressionValue getValue()
    {
        return value;
    }

    @Override
    public void loadExpression(DebuggeeContext debuggeeContext) throws ExpressionException
    {
        throw new ExpressionException("The value for this expression is already available");
    }

    @Override
    public boolean isExpressionCompleted(DebuggeeContext debuggeeContext) throws ExpressionException
    {
        return true;
    }
}
