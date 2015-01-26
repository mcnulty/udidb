/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.events;

import net.libudi.api.event.UdiEvent;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.ops.OperationException;

/**
 * An observer for an event dispatched by an EventDispatcher
 *
 * @author mcnulty
 */
public interface EventObserver {

    /**
     * @return the debuggee whose events are of interest to this observer
     */
    DebuggeeContext getDebuggeeContext();

    /**
     * Notifies this observer of an event
     *
     * @param event the event
     *
     * @return true, if this observer should continue to receive events for the corresponding debuggee; false otherwise
     *
     * @throws OperationException indicates the observer failed to process the event
     */
    boolean publish(UdiEvent event) throws OperationException;
}
