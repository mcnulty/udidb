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
public enum ValueType
{
    CHAR("char"),
    NUMBER("number"),
    STRING("string"),
    ADDRESS("address");

    private final String displayName;

    private ValueType(String displayName)
    {
        this.displayName = displayName;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
