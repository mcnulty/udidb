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

    private final String description;
    private final Object value;

    /**
     * Constructor.
     *
     * @param description a description of this result
     * @param value a machine consumable value
     */
    public ValueResult(String description, Object value)
    {
        this.description = description;
        this.value = value;
    }

    public ValueResult(Object value)
    {
        this.description = value.toString();
        this.value = value;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return this.value;
    }

    /**
     * @return the description of this result
     */
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public String toString() {
        return description;
    }

    @Override
    public boolean accept(Operation op, OperationResultVisitor visitor) {
        return visitor.visit(op, this);
    }
}
