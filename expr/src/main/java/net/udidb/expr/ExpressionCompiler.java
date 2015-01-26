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
 * Interface that provides a mechanism to compile an expression in a source language, suitable for evaluation directly
 * by the debugger or indirectly by the debuggee
 *
 * @author mcnulty
 */
public interface ExpressionCompiler
{

    /**
     * Compiles an expression
     *
     * @param expression the expression
     * @param debuggeeContext the debuggee context in which the expression will be evaluated
     *
     * @return the handle to the expression, used to evaluate the expression
     *
     * @throws ExpressionException on failure to compile the expression
     */
    Expression compile(String expression, DebuggeeContext debuggeeContext) throws ExpressionException;
}
