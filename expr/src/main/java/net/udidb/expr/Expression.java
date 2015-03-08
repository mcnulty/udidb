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
 * Handle to an expression returned by an {@link ExpressionCompiler}
 *
 * @author dmcnulty
 */
public interface Expression
{
    /**
     * @return the value of the expression, if known or null if not known and execution is required
     */
    ExpressionValue getValue();

    /**
     * Loads the expression into the debuggee for execution
     *
     * @param executionContext the debuggee context
     *
     * @throws ExpressionException on failure to load the expression into the debuggee
     */
    void loadExpression(ExecutionContext executionContext) throws ExpressionException;

    /**
     * Determines whether the expression has completed execution
     *
     * @return true if the expression has completed execution
     */
    boolean isExpressionCompleted();
}
