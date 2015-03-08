/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr.values;

/**
 * @author mcnulty
 */
public abstract class BaseExpressionValue<T> implements ExpressionValue
{
    protected final T value;

    protected final ValueType type;

    protected BaseExpressionValue(T value, ValueType type)
    {
        this.value = value;
        this.type = type;
    }

    @Override
    public Number getNumberValue()
    {
        throw new IllegalStateException("Cannot convert " + value + " into number");
    }

    @Override
    public char getCharValue()
    {
        throw new IllegalStateException("Cannot convert " + value + " into character");
    }

    @Override
    public String getStringValue()
    {
        throw new IllegalStateException("Cannot convert " + value + " into string");
    }

    @Override
    public long getAddressValue()
    {
        throw new IllegalStateException("Cannot convert " + value + " into address");
    }

    @Override
    public ValueType getType()
    {
        return type;
    }

    @Override
    public String toString()
    {
        return value.toString();
    }
}
