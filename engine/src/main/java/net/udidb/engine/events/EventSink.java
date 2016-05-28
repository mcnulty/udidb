/*
 * Copyright (c) 2011-2016, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.events;

import java.util.List;

import net.libudi.api.event.UdiEvent;

/**
 * An interface that provides a sink for events read by the EventPump
 *
 * @author mcnulty
 */
public interface EventSink
{
    void accept(List<UdiEvent> events);
}
