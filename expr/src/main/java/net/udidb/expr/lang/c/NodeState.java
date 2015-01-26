/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c;

import net.sourcecrumbs.api.debug.symbols.DebugType;
import net.sourcecrumbs.api.debug.symbols.Function;
import net.sourcecrumbs.api.debug.symbols.Variable;
import net.sourcecrumbs.api.symbols.Symbol;

/**
 * Container for all node state specific to the expression evaluator
 *
 * @author mcnulty
 */
public class NodeState
{
    private Variable variable;

    private Symbol symbol;

    private Function function;

    private DebugType effectiveType;

    public Symbol getSymbol()
    {
        return symbol;
    }

    public void setSymbol(Symbol symbol)
    {
        this.symbol = symbol;
    }

    public Variable getVariable()
    {
        return variable;
    }

    public void setVariable(Variable variable)
    {
        this.variable = variable;
    }

    public Function getFunction()
    {
        return function;
    }

    public void setFunction(Function function)
    {
        this.function = function;
    }

    public DebugType getEffectiveType()
    {
        return effectiveType;
    }

    public void setEffectiveType(DebugType effectiveType)
    {
        this.effectiveType = effectiveType;
    }
}
