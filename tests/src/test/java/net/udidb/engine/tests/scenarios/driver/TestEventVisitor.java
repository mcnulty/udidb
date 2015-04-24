/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.engine.tests.scenarios.driver;

import net.libudi.api.event.EventType;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.event.UdiEventBreakpoint;
import net.libudi.api.event.UdiEventError;
import net.libudi.api.event.UdiEventProcessCleanup;
import net.libudi.api.event.UdiEventProcessExit;
import net.libudi.api.event.UdiEventThreadCreate;
import net.libudi.api.event.UdiEventVisitor;

import static org.junit.Assert.assertEquals;

/**
 * @author mcnulty
 */
public class TestEventVisitor implements UdiEventVisitor
{
    private EventType currentExpectedEventType;

    public EventType getCurrentExpectedEventType()
    {
        return currentExpectedEventType;
    }

    public void setCurrentExpectedEventType(EventType currentExpectedEventType)
    {
        this.currentExpectedEventType = currentExpectedEventType;
    }

    private void checkEvent(UdiEvent event)
    {
        assertEquals(currentExpectedEventType, event.getEventType());
    }

    @Override
    public void visit(UdiEventBreakpoint breakpointEvent)
    {
        checkEvent(breakpointEvent);
    }

    @Override
    public void visit(UdiEventError errorEvent)
    {
        checkEvent(errorEvent);
    }

    @Override
    public void visit(UdiEventProcessExit processExitEvent)
    {
        checkEvent(processExitEvent);
    }

    @Override
    public void visit(UdiEventThreadCreate threadCreateEvent)
    {
        checkEvent(threadCreateEvent);
    }

    @Override
    public void visit(UdiEventProcessCleanup processCleanup)
    {
        checkEvent(processCleanup);
    }
}
