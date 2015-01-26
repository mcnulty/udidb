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
 * Result that indicates the Operation executed successfully but did not produce any output.
 *
 * @author mcnulty
 */
public class VoidResult extends BaseResult {

    /**
     * Constructor.
     */
    public VoidResult() {
    }

    /**
     * Constructor.
     *
     * @param eventPending whether or not an event is pending as a result of the operation
     */
    public VoidResult(boolean eventPending) {
        this.eventPending = eventPending;
    }

    @Override
    public boolean accept(Operation op, OperationResultVisitor visitor) {
        return visitor.visit(op, this);
    }
}
