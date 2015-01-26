/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.ops.results;

import net.udidb.engine.events.EventObserver;
import net.udidb.engine.ops.Operation;

/**
 * A result that indicates the operation's result is not yet available
 *
 * @author mcnulty
 */
public class DeferredResult extends BaseResult {

    /**
     * Constructor.
     *
     * <p>
     *     This constructor implies an event is pending
     * </p>
     *
     * @param deferredEventObserver the event observer
     */
    public DeferredResult(EventObserver deferredEventObserver) {
        this.eventPending = true;
        this.deferredEventObserver = deferredEventObserver;
    }

    @Override
    public boolean accept(Operation op, OperationResultVisitor visitor) {
        return visitor.visit(op, this);
    }
}
