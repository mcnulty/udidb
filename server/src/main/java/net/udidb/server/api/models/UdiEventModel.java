/*
 * Copyright (c) 2011-2015, Dan McNulty
 * All rights reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.udidb.server.api.models;

import java.util.HashMap;
import java.util.Map;

import net.libudi.api.event.EventType;
import net.libudi.api.event.UdiEvent;
import net.libudi.api.event.UdiEventBreakpoint;
import net.libudi.api.event.UdiEventError;
import net.libudi.api.event.UdiEventProcessCleanup;
import net.libudi.api.event.UdiEventProcessExit;
import net.libudi.api.event.UdiEventThreadCreate;
import net.libudi.api.event.UdiEventVisitor;
import net.udidb.engine.context.DebuggeeContext;

/**
 * @author mcnulty
 */
public class UdiEventModel
{
    private String contextId;

    private int pid;

    private long tid;

    private EventType eventType;

    private Map<String, Object> eventData = new HashMap<>();

    public UdiEventModel()
    {
    }

    public UdiEventModel(UdiEvent udiEvent)
    {
        pid = udiEvent.getProcess().getPid();

        tid = udiEvent.getThread() != null ? udiEvent.getThread().getTid() : 0L;

        eventType = udiEvent.getEventType();

        populateEventData(udiEvent);

        Object processData = udiEvent.getProcess().getUserData();
        if (processData instanceof DebuggeeContext)
        {
            contextId = ((DebuggeeContext) processData).getId();
        }
    }

    private void populateEventData(UdiEvent udiEvent)
    {
        udiEvent.accept(new UdiEventVisitor()
        {

            @Override
            public void visit(UdiEventBreakpoint breakpointEvent)
            {
                eventData.put("address", breakpointEvent.getAddress());
            }

            @Override
            public void visit(UdiEventError errorEvent)
            {
                eventData.put("errorString", errorEvent.getErrorString());
            }

            @Override
            public void visit(UdiEventProcessExit processExitEvent)
            {
                eventData.put("exitCode", processExitEvent.getExitCode());
            }

            @Override
            public void visit(UdiEventThreadCreate threadCreateEvent)
            {
                eventData.put("newThreadId", threadCreateEvent.getNewThread().getTid());
            }

            @Override
            public void visit(UdiEventProcessCleanup processCleanup)
            {
            }
        });
    }

    public String getContextId()
    {
        return contextId;
    }

    public void setContextId(String contextId)
    {
        this.contextId = contextId;
    }

    public Map<String, Object> getEventData()
    {
        return eventData;
    }

    public void setEventData(Map<String, Object> eventData)
    {
        this.eventData = eventData;
    }

    public EventType getEventType()
    {
        return eventType;
    }

    public void setEventType(EventType eventType)
    {
        this.eventType = eventType;
    }

    public int getPid()
    {
        return pid;
    }

    public void setPid(int pid)
    {
        this.pid = pid;
    }

    public long getTid()
    {
        return tid;
    }

    public void setTid(long tid)
    {
        this.tid = tid;
    }
}