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

/**
 * Abstract base class containing fields for a Result
 *
 * @author mcnulty
 */
public abstract class BaseResult implements Result {

    protected boolean eventPending = false;

    protected EventObserver deferredEventObserver = null;

    @Override
    public boolean isEventPending() {
        return eventPending;
    }

    @Override
    public EventObserver getDeferredEventObserver() {
        return deferredEventObserver;
    }
}
