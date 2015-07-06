/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.engine;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Singleton;

import net.libudi.api.UdiProcess;
import net.udidb.engine.events.EventObserver;

/**
 * @author mcnulty
 */
@Singleton
public class ServerEventDispatcher
{
    private final Map<UdiProcess, Set<EventObserver>> eventObservers = new HashMap<>();

    public void registerEventObserver(EventObserver eventObserver) {
        Set<EventObserver> procEventObs;
        synchronized (eventObservers) {
            procEventObs = eventObservers.get(eventObserver.getDebuggeeContext().getProcess());
            if (procEventObs == null) {
                procEventObs = new HashSet<>();
                eventObservers.put(eventObserver.getDebuggeeContext().getProcess(), procEventObs);
            }
        }
        procEventObs.add(eventObserver);
    }
}
