/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.results;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.udidb.engine.events.EventObserver;
import net.udidb.engine.ops.results.ValueResult;

/**
 * @author mcnulty
 */
public class ValueResultMixIn
{
    private boolean eventPending = false;

    @JsonIgnore
    private EventObserver deferredEventObserver = null;

    private String description;

    private Object value;

    private String typeName = ValueResult.class.getSimpleName();

}
