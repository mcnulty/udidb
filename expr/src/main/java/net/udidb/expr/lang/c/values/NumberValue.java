/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.lang.c.values;

/**
 * @author mcnulty
 */
public class NumberValue extends BaseExpressionValue<Number>
{
    public NumberValue(Number value)
    {
        super(value);
    }

    @Override
    public Number getNumberValue()
    {
        return value;
    }
}
