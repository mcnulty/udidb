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
import net.libudi.api.exceptions.UdiException;
import net.udidb.engine.context.DebuggeeContext;
import net.udidb.engine.events.DbEventData;
import net.udidb.engine.ops.OperationException;

/**
 * @author mcnulty
 */
public class UdiEventModel
{
    private String contextId;

    private String pid;

    private String tid;

    private EventType eventType;

    private Map<String, Object> eventData = new HashMap<>();

    private boolean intermediateEvent = false;

    public UdiEventModel()
    {
    }

    public UdiEventModel(UdiEvent udiEvent) throws OperationException
    {
        try {
            pid = Integer.toString(udiEvent.getProcess().getPid());

            tid = Long.toHexString(udiEvent.getThread() != null ? udiEvent.getThread().getTid() : 0L);
        } catch (UdiException e) {
            throw new OperationException(e);
        }

        eventType = udiEvent.getEventType();

        populateEventData(udiEvent);

        Object processData = udiEvent.getProcess().getUserData();
        if (processData instanceof DebuggeeContext) {
            contextId = ((DebuggeeContext) processData).getId();
        }

        Object userData = udiEvent.getUserData();
        if (userData instanceof DbEventData) {
            intermediateEvent = ((DbEventData) userData).isIntermediateEvent();
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
                try {
                    eventData.put("newThreadId", threadCreateEvent.getNewThread().getTid());
                } catch (UdiException e) {
                    throw new RuntimeException("Failed to determine new thread id", e);
                }
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

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }

    public String getTid()
    {
        return tid;
    }

    public void setTid(String tid)
    {
        this.tid = tid;
    }

    public boolean isIntermediateEvent()
    {
        return intermediateEvent;
    }

    public void setIntermediateEvent(boolean intermediateEvent)
    {
        this.intermediateEvent = intermediateEvent;
    }
}
