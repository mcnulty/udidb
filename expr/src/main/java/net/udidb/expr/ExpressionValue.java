/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.expr;

/**
 * Represents a value (or subvalue) of an expression.
 *
 * Note: implementations may throw IllegalStateExceptions for any method calls if the underlying data cannot be converted
 * into the requested type. The intent is that an earlier type checking pass will eliminate expressions for which invalid
 * type conversions would occur.
 *
 * @author mcnulty
 */
public interface ExpressionValue
{
    Number getNumberValue();

    char getCharValue();

    String getStringValue();

    long getAddressValue();
}
