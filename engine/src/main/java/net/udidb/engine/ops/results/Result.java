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
 * Encapsulates all new information made available by the execution of an operation.
 *
 * Note: all Result implementations should provide a non-default toString method. This will be used to obtain a
 * String representation of the the result.
 *
 * @author mcnulty
 */
public interface Result {

    /**
     * Accept a visitor to visit the result
     *
     * @param op the operation for which this is a result
     * @param visitor the visitor
     *
     * @return true, if the result indicates further operations should be executed; false otherwise
     */
    boolean accept(Operation op, OperationResultVisitor visitor);

    /**
     * @return true, if events are expected due to the execution of the operation
     */
    boolean isEventPending();

    /**
     * Operations can defer their completion until an event occurs in a debuggee. This method allows an Operation to
     * create a Result that encapsulates a visitor that can be used to complete the operation after an event has
     * occurred.
     *
     * @return the event observer for the operation, or null if the event has not been deferred
     */
    EventObserver getDeferredEventObserver();
}
