/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.results;

import net.udidb.engine.ops.Operation;

/**
 * A result that encapsulates a single value
 */
public class ValueResult extends BaseResult {

    private final Object value;

    /**
     * Constructor.
     *
     * @param value the value
     */
    public ValueResult(Object value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public boolean accept(Operation op, OperationResultVisitor visitor) {
        return visitor.visit(op, this);
    }
}
