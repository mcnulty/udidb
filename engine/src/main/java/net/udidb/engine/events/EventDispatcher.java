/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.events;

import net.udidb.engine.ops.OperationException;
import net.udidb.engine.ops.results.Result;

/**
 * Provides methods to dispatch and handle events that occur in a process
 *
 * @author mcnulty
 */
public interface EventDispatcher {

    /**
     * Handle any pending events in all managed processes. Whether or not this call blocks is dependent on the implementation.
     *
     * @param result the result of the operation that could trigger events
     *
     * @throws OperationException on failure to handle the events
     */
    void handleEvents(Result result) throws OperationException;
}
